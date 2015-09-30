##Save Henplus Plug-In##

This plugin allows you to save the results of a SELECT statement to multiple files. The data is saved so that every column of every 
row is saved two a separate file. There are two commands allowing you to save everything using a single file prefix, or to a hierarchy
of folders based on a folder prefix.


###Easy Setup###

Simply put `save-henplus-plugin.jar` in to the CLASSPATH of `henplus`, generally in the `share/henplus` folder somewhere.

Start `henplus` and register the plugin. Use the `plug-in` command for this. This only needs to be done once, and will be persisted.

     Hen*Plus> plug-in org.fakebelieve.henplus.plugins.save.SaveCommand

###Usage###

The plugin responds to two commands `save-file` and `save-folder`.

*File-based output with `save-file` command*

The `save-file` command will save all of the data to multiple files rooted a a single file prefix. All the files will appear
in a single directory from the base of the prefix.

*Folder-based output with `save-folder` command*

The `save-folder` command will save all the data to multiple files broken down into separate folders, one folder for each row.
