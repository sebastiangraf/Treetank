package org.treetank.filelistener.ui.dialogs;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ListenToFolderDialog extends Dialog {

    protected Object result;
    protected Shell shell;
    private Label lblFolder;
    private Button btnUseAnExisting;
    private Button btnChooseStorageConfiguration;
    private Button button;
    private Button btnCreateANew;
    private Button btnSubmit;
    private Button btnCancel;

    /**
     * Properties for the storage to listen to
     */
    private Text savePath;
    private String listenFolder;
    private String storageName;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public ListenToFolderDialog(Shell parent, int style) {
        super(parent, style);
        setText("Please configure which folder you want to listen to");
    }

    /**
     * Open the dialog.
     * 
     * @return the result
     */
    public Object open() {
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(450, 218);
        shell.setText(getText());

        lblFolder = new Label(shell, SWT.NONE);
        lblFolder.setBounds(10, 10, 70, 17);
        lblFolder.setText("Folder:");

        savePath = new Text(shell, SWT.BORDER);
        savePath.setBounds(86, 10, 298, 27);

        button = new Button(shell, SWT.NONE);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnFolderDialog();
            }
        });
        button.setImage(SWTResourceManager.getImage(ListenToFolderDialog.class,
            "/com/sun/java/swing/plaf/gtk/icons/Directory.gif"));
        button.setBounds(390, 10, 52, 29);

        btnUseAnExisting = new Button(shell, SWT.CHECK);
        btnUseAnExisting.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                do_btnUseAnExisting_widgetSelected(e);
            }
        });
        btnUseAnExisting.setBounds(10, 43, 430, 24);
        btnUseAnExisting.setText("Use an existing storage configuration?");

        btnChooseStorageConfiguration = new Button(shell, SWT.NONE);
        btnChooseStorageConfiguration.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnChooseStorageConfiguration();
            }
        });
        btnChooseStorageConfiguration.setBounds(10, 71, 428, 29);
        btnChooseStorageConfiguration.setText("Choose storage configuration");
        btnChooseStorageConfiguration.setEnabled(false);

        btnCreateANew = new Button(shell, SWT.NONE);
        btnCreateANew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnCreateStorageconfiguration();
            }
        });
        btnCreateANew.setBounds(10, 106, 428, 29);
        btnCreateANew.setText("Create a new storage configuration");

        btnSubmit = new Button(shell, SWT.NONE);
        btnSubmit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnSubmit();
            }
        });
        btnSubmit.setBounds(10, 141, 91, 29);
        btnSubmit.setText("Submit");

        btnCancel = new Button(shell, SWT.NONE);
        btnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnCancel();
            }
        });
        btnCancel.setBounds(107, 141, 91, 29);
        btnCancel.setText("Cancel");

    }

    private void btnFolderDialog() {
        DirectoryDialog dialog = new DirectoryDialog(shell);
        dialog.setText("Choose a folder");
        String platform = SWT.getPlatform();
        String homefolder = System.getProperty("user.home");
        dialog.setFilterPath(homefolder);

        dialog.open();

        savePath.setText(dialog.getFilterPath());
        this.storageName = savePath.getText();
    }

    protected void do_btnUseAnExisting_widgetSelected(final SelectionEvent e) {
        if (btnUseAnExisting.getSelection()) {
            this.btnCreateANew.setEnabled(false);
            this.btnChooseStorageConfiguration.setEnabled(true);
        } else {
            this.btnCreateANew.setEnabled(true);
            this.btnChooseStorageConfiguration.setEnabled(false);
        }
    }

    private void btnChooseStorageConfiguration() {
        ChooseExisitingStorageDialog dialog = new ChooseExisitingStorageDialog(new Shell(), SWT.DIALOG_TRIM);
        dialog.open();

        this.storageName = dialog.getStorageName();
    }

    private void btnCreateStorageconfiguration() {
        CreateStorageConfigurationDialog dialog =
            new CreateStorageConfigurationDialog(new Shell(), SWT.DIALOG_TRIM);
        dialog.open();

        this.storageName = dialog.getName();
    }

    private void btnSubmit() {
        this.getParent().close();
    }

    private void btnCancel() {
        this.shell.dispose();
    }

    public String getListenFolder() {
        return listenFolder;
    }

    public String getStorageName() {
        return storageName;
    }

}