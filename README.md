##Catalog Henplus Plug-In##

This plugin allows you to change the default database/catalog the session is set to use. Its very similar to the MySQL `use database` command, only with a more generic JDBC nomenclature.

###Easy Setup###

Simply put `catalog-henplus-plugin.jar` in to the CLASSPATH of `henplus`, generally in the `share/henplus` folder somewhere.

Start `henplus` and register the plugin. Use the `plug-in` command for this. This only needs to be done once, and will be persisted.

     Hen*Plus> plug-in org.fakebelieve.henplus.plugins.catalog.CatalogCommand

###Usage###

The plugin responds to two commands `catalog` and `catalogs`.

*Listing databases with the `catalogs` command*

The `catalogs` command will list all of the available database/catalogs for the current session.

*Displaying the current database with the `catalog` command*

The `catalog` command with no arguments will display the current database/catalog for the current session.

*Changing the database with the `catalog` command*

The `catalog` command takes an optional parameter. If specified, this is the database/catalog you want to set as the default for the current session. Partial database completion is available by hitting TAB.
