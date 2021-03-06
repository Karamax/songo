package songo.view;

import com.google.inject.Inject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import songo.ResourceUtil;

public class TrayView {
	private final TrayItem item;
	private MenuItem exit;

	@Inject
	TrayView(Display display, ResourceUtil resourceUtil) {
		item = new TrayItem(display.getSystemTray(), SWT.NONE);
		item.setImage(new Image(display, resourceUtil.iconStream("tray")));
	}

	void setShell(Shell shell) {
		final Menu menu = new Menu(shell, SWT.POP_UP);
		exit = new MenuItem(menu, SWT.NONE);
		exit.setText("Exit");
		item.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(MenuDetectEvent menuDetectEvent) {
				menu.setVisible(true);
			}
		});
	}

	void addExitListener(final Runnable listener) {
		exit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				listener.run();
			}
		});
	}

	void addRestoreListener(final Runnable listener) {
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				listener.run();
			}
		});
	}
}
