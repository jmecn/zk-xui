package jcurses.widgets;

import java.io.FileFilter;

public interface JCursesFileFilterFactory {
	
	FileFilter generateFileFilter(String filterString);
}