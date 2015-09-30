/*
 * This is free software, licensed under the Gnu Public License (GPL)
 * get a copy from <http://www.gnu.org/licenses/gpl.html>
 */
package org.fakebelieve.henplus.plugins.save;

import henplus.AbstractCommand;
import henplus.CommandDispatcher;
import henplus.HenPlus;
import henplus.SQLSession;
import henplus.commands.TimeRenderer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public final class SaveCommand extends AbstractCommand {

    private static final String COMMAND_SAVE_FILE = "save-file";
    private static final String COMMAND_SAVE_FOLDER = "save-folder";

    /**
     *
     */
    public SaveCommand() {
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#getCommandList()
     */
    @Override
    public String[] getCommandList() {
        return new String[]{COMMAND_SAVE_FILE, COMMAND_SAVE_FOLDER};
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#participateInCommandCompletion()
     */
    @Override
    public boolean participateInCommandCompletion() {
        return true;
    }

    protected List<String> getCatalogs(SQLSession session) throws SQLException {
        List<String> list = new ArrayList<String>();
        ResultSet catalogs = session.getConnection().getMetaData().getCatalogs();
        while (catalogs.next()) {
            list.add(catalogs.getString(1));
        }
        catalogs.close();

        return list;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#execute(henplus.SQLSession, java.lang.String, java.lang.String)
     */

    @Override
    public int execute(SQLSession session, String command, String parameters) {
        int result = SUCCESS;

        // required: session
        if (session == null) {
            HenPlus.msg().println("You need a valid session for this command.");
            return EXEC_FAILED;
        }

        if (command.equals(COMMAND_SAVE_FILE) || command.equals(COMMAND_SAVE_FOLDER)) {
            final StringTokenizer st = new StringTokenizer(parameters);
            final int argc = st.countTokens();

            if (argc < 2) {
                return SYNTAX_ERROR;
            }

            boolean folderMode = command.equals(COMMAND_SAVE_FOLDER);

            String filePrefix = st.nextToken().trim();
            String selectSql = st.nextToken("").trim();

            session.println("Saving to " + filePrefix);

            Statement statement = session.createStatement();
            ResultSet resultSet = null;
            try {
                resultSet = statement.executeQuery(selectSql);
            } catch (SQLException e) {
                HenPlus.msg().println(e.getMessage());
                return EXEC_FAILED;
            }

            ResultSetMetaData metaData = null;
            int rows;
            String[] columnNames;
            try {
                metaData = resultSet.getMetaData();
                rows = metaData.getColumnCount();
                columnNames = new String[rows];
                for (int idx = 0; idx < rows; idx++) {
                    columnNames[idx] = metaData.getColumnLabel(idx + 1);
                }
            } catch (SQLException e) {
                HenPlus.msg().println(e.getMessage());
                return EXEC_FAILED;
            }


            try {
                final long startTime = System.currentTimeMillis();
                long lapTime = -1;
                long execTime = -1;

                for (int count = 1; resultSet.next(); count++) {
                    for (int idx = 0; idx < rows; idx++) {
                        Object field = resultSet.getObject(idx + 1);
                        String fileName;
                        if (folderMode) {
                            String dirName = String.format("%s/%d", filePrefix, count);
                            fileName = String.format("%s/%d/%s.out", filePrefix, count, columnNames[idx]);
                            File dir = new File(dirName);
                            dir.mkdirs();
                        } else {
                            fileName = String.format("%s.%d.%s.out", filePrefix, count, columnNames[idx]);
                        }
                        BufferedWriter out = new BufferedWriter(new FileWriter(fileName, false));
                        if (field != null) {
                            out.write(field.toString());
                        }
                        out.close();
                    }
                    lapTime = System.currentTimeMillis() - startTime;
                }

                session.println(rows + " row" + (rows == 1 ? "" : "s") + " in result");

                execTime = System.currentTimeMillis() - startTime;
                session.print(" (");
                if (lapTime > 0) {
                    session.print("first row: ");
                    if (session.printMessages()) {
                        TimeRenderer.printTime(lapTime, HenPlus.msg());
                    }
                    session.print("; total: ");
                }
                if (session.printMessages()) {
                    TimeRenderer.printTime(execTime, HenPlus.msg());
                }
                session.println(")");

            } catch (SQLException e) {
                HenPlus.msg().println(e.getMessage());
                return EXEC_FAILED;
            } catch (IOException e) {
                HenPlus.msg().println(e.getMessage());
                return EXEC_FAILED;
            }

        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#isComplete(java.lang.String)
     */
    @Override
    public boolean isComplete(String command) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#requiresValidSession(java.lang.String)
     */
    @Override
    public boolean requiresValidSession(String cmd) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#shutdown()
     */
    @Override
    public void shutdown() {
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#getShortDescription()
     */
    @Override
    public String getShortDescription() {
        return "save results to multiple files";
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#getSynopsis(java.lang.String)
     */
    @Override
    public String getSynopsis(String cmd) {
        if (cmd.equals(COMMAND_SAVE_FILE)) {
            return cmd + " <file-prefix> select ...";
        }
        if (cmd.equals(COMMAND_SAVE_FOLDER)) {
            return cmd + " <folder-prefix> select ...";
        }
        return cmd;
    }

    /*
     * (non-Javadoc)
     * @see henplus.Command#getLongDescription(java.lang.String)
     */
    @Override
    public String getLongDescription(String cmd) {
        if (cmd.equals(COMMAND_SAVE_FILE)) {
            return "\tSave the output of a SELECT to files.\n"
                    + "\tFiles will be named <file-prefix>.<row-number>.<column-name>.out\n"
                    + "\n"
                    + "\t\t" + COMMAND_SAVE_FILE + " <file-prefix> select ...;\n"
                    + "\n";
        }
        if (cmd.equals(COMMAND_SAVE_FOLDER)) {
            return "\tSave the output of a SELECT to folders of files\n"
                    + "\tFiles will be organized into folders named <folder-prefix>/<row-number>/<column-name>.out\n"
                    + "\n"
                    + "\t\t" + COMMAND_SAVE_FOLDER + " <folder-prefix> select ...;\n"
                    + "\n";
        }
        return null;
    }

    @Override
    public Iterator complete(CommandDispatcher disp, String partialCommand, String lastWord) {
        HenPlus.getInstance().getCurrentSession();

        try {
            List<String> catalogs = getCatalogs(HenPlus.getInstance().getCurrentSession());
            for (Iterator<String> i = catalogs.listIterator(); i.hasNext(); ) {
                String catalog = i.next();
                if (!catalog.startsWith(lastWord)) {
                    i.remove();
                }
            }
            return catalogs.iterator();
        } catch (SQLException ex) {
            HenPlus.msg().println("Problem - " + ex.getMessage());
            return super.complete(disp, partialCommand, lastWord);
        }

    }
}
