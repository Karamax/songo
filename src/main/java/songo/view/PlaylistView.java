package songo.view;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import songo.TableUtil;
import songo.annotation.PlaylistTab;
import songo.model.Playlist;
import songo.vk.Audio;

import java.util.List;

public class PlaylistView extends Composite {
	private final Table table;
	private final Playlist playlist;
	private final Font boldFont;
	private final MenuItem delete;
	private final Font normalFont;

	@Inject
	PlaylistView(@PlaylistTab TabItem playlistTab, Playlist playlist, final EventBus bus) {
		super(playlistTab.getParent(), SWT.NONE);
		this.playlist = playlist;
		playlistTab.setControl(this);
		setLayout(new FillLayout());
		table = new Table(this, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		String[] columns = new String[]{"Artist", "Title"};
		for (String name : columns) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(name);
			column.pack();
		}
		FontData[] data = table.getFont().getFontData();
		normalFont = new Font(getDisplay(), data);
		for (FontData d : data)
			d.setStyle(SWT.BOLD);
		boldFont = new Font(getDisplay(), data);
		DragSource dragSource = new DragSource(table, DND.DROP_MOVE);
		dragSource.setTransfer(new Transfer[]{TextTransfer.getInstance()});
		dragSource.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = "track";
			}
		});
		final DropTarget dropTarget = new DropTarget(table, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[]{TextTransfer.getInstance()});
		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				for (TransferData t : event.dataTypes)
					if (TextTransfer.getInstance().isSupportedType(t))
						event.currentDataType = t;
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_INSERT_BEFORE | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (event.item == null)
					return;
				Audio track = (Audio) event.item.getData("track");
				bus.post(new InsertBefore(table.getSelectionIndices(), table.indexOf((TableItem) event.item)));
				int index = 0;
				for (Item item : table.getItems()) {
					if (item.getData("track") == track)
						break;
					index++;
				}
				int start = index - table.getSelection().length, end = index - 1;
				table.deselectAll();
				table.select(start, end);
			}
		});
		Menu menu = new Menu(table);
		delete = new MenuItem(menu, SWT.NONE);
		delete.setText("Remove");
		table.setMenu(menu);
		updateTable();
	}

	public void updateTable() {
		table.setRedraw(false);
		int[] selection = table.getSelectionIndices();
		table.setItemCount(playlist.getTracks().size());
		int i = 0;
		for (Audio track : playlist.getTracks()) {
			TableItem item = table.getItem(i);
			item.setFont(normalFont);
			if (i == playlist.getCurrentTrackIndex())
				item.setFont(boldFont);
			item.setText(new String[]{track.artist, track.title});
			item.setData("track", track);
			i++;
		}
		TableUtil.packColumns(table);
		table.select(selection);
		table.setRedraw(true);
	}

	public int[] getSelectedIndices() {
		return table.getSelectionIndices();
	}

	public List<Audio> getSelectedTracks() {
		List<Audio> tracks = playlist.getTracks();
		ImmutableList.Builder<Audio> result = new ImmutableList.Builder<Audio>();
		for (int i : table.getSelectionIndices())
			result.add(tracks.get(i));
		return result.build();
	}

	public void addPlayListener(final Runnable listener) {
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				listener.run();
			}
		});
	}

	public static class InsertBefore {
		public final int[] source;
		public final int target;

		private InsertBefore(int[] source, int target) {
			this.source = source;
			this.target = target;
		}
	}

	public void addDelListener(final Runnable listener) {
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.DEL) {
					listener.run();
					table.deselectAll();
				}
			}
		});
		delete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				listener.run();
				table.deselectAll();
			}
		});
	}
}
