package com.github.mdolk.ng.netbeans.codecompletion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class NgDocCache {
	private final Map<String, List<NgDoc>> cache = new HashMap<String, List<NgDoc>>();

	public NgDocCache() {
		FileUtil.addFileChangeListener(new UpdateCacheWhenJsFileChangedListener());
	}

	private class UpdateCacheWhenJsFileChangedListener extends FileChangeAdapter {
		@Override
		public void fileDataCreated(FileEvent fe) {
			updateCache(fe);
		}

		@Override
		public void fileChanged(FileEvent fe) {
			updateCache(fe);
		}

		@Override
		public void fileDeleted(FileEvent fe) {
			updateCache(fe);
		}

		private void updateCache(FileEvent fe) {
			FileObject file = fe.getFile();
			if (!"text/javascript".equals(file.getMIMEType())) {
				return;
			}
			Project project = FileOwnerQuery.getOwner(file);
			if (project == null) {
				return;
			}
			ProjectInformation info = ProjectUtils.getInformation(project);
			System.out.println("Update ngdoc cache for project '" + info.getDisplayName() + "'");
			cache.put(info.getName(), readProjectNgDocsFromFiles(project));
		}
	}

	public List<NgDoc> getNgDocs(Project project) {
		ProjectInformation info = ProjectUtils.getInformation(project);
		String projectName = info.getName();
		if (!cache.containsKey(projectName)) {
			System.out.println("Init ngdoc cache for project '" + info.getDisplayName() + "'");
			cache.put(projectName, readProjectNgDocsFromFiles(project));
		}
		return cache.get(projectName);
	}

	private static List<NgDoc> readProjectNgDocsFromFiles(Project project) {
		List<FileObject> jsFiles = NgUtils.findByMimeType(project.getProjectDirectory(), "text/javascript");
		List<NgDoc> result = new ArrayList<NgDoc>();
		for (FileObject jsFile : jsFiles) {
			System.out.println(" -- " + jsFile.getPath());
			try {
				for (String docComment : NgUtils.getDocComments(jsFile.asText()) ) {
					NgDoc ngDoc = new NgDoc(docComment);
					if (ngDoc.isPresent("@ngdoc")) {
						result.add(ngDoc);
					}
				}
			} catch (IOException ex) {
				Exceptions.printStackTrace(ex);
			}
		}		
		return result;
	}
}
