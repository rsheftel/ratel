using System;
using System.Drawing;
using System.Drawing.Printing;
using System.Reflection;
using System.Resources;
using System.Windows.Forms;
using Q.Util;

namespace ZedGraph {
/*
	/// <summary>
	/// 
	/// </summary>
	public struct DrawingThreadData
	{
		/// <summary>
		/// 
		/// </summary>
		public Graphics _g;
		/// <summary>
		/// 
		/// </summary>
		public MasterPane _masterPane;

//		public DrawingThread( Graphics g, MasterPane masterPane )
//		{
//			_g = g;
//			_masterPane = masterPane;
//		}
	}
*/

    /// <summary>
    /// The ZedGraphControl class provides a UserControl interface to the
    /// <see cref="ZedGraph"/> class library.  This allows ZedGraph to be installed
    /// as a control in the Visual Studio toolbox.  You can use the control by simply
    /// dragging it onto a form in the Visual Studio form editor.  All graph
    /// attributes are accessible via the <see cref="ZedGraphControl.GraphPane"/>
    /// property.
    /// </summary>
    /// <author> John Champion revised by Jerry Vos </author>
    /// <version> $Revision: 3.86 $ $Date: 2007-11-03 04:41:29 $ </version>
    public partial class ZedGraphControl : UserControl {
        #region Private Fields
        const int _ScrollControlSpan = int.MaxValue;
        // The ratio of the largeChange to the smallChange for the scroll bars
        const int _ScrollSmallRatio = 10;
        readonly ResourceManager _resourceManager;
        /// <summary>
        /// This private field contains a list of selected CurveItems.
        /// </summary>
        //private List<CurveItem> _selection = new List<CurveItem>();
        readonly Selection _selection = new Selection();
        readonly ScrollRangeList _y2ScrollRangeList;
        readonly ScrollRangeList _yScrollRangeList;

        /// <summary>
        /// private value that determines whether or not panning is allowed for the control in the
        /// horizontal direction.  Use the
        /// public property <see cref="IsEnableHPan"/> to access this value.
        /// </summary>
        bool _isEnableHPan = true;
        /// <summary>
        /// private value that determines whether or not zooming is enabled for the control in the
        /// horizontal direction.  Use the public property <see cref="IsEnableHZoom"/> to access this
        /// value.
        /// </summary>
        bool _isEnableHZoom = true;

        /// <summary>
        /// private value that determines whether or not panning is allowed for the control in the
        /// vertical direction.  Use the
        /// public property <see cref="IsEnableVPan"/> to access this value.
        /// </summary>
        bool _isEnableVPan = true;
        /// <summary>
        /// private value that determines whether or not zooming is enabled for the control in the
        /// vertical direction.  Use the public property <see cref="IsEnableVZoom"/> to access this
        /// value.
        /// </summary>
        bool _isEnableVZoom = true;
        /// <summary>
        /// private value that determines whether or not zooming is enabled with the mousewheel.
        /// Note that this property is used in combination with the <see cref="IsEnableHZoom"/> and
        /// <see cref="IsEnableVZoom" /> properties to control zoom options.
        /// </summary>
        bool _isEnableWheelZoom = true;
        /// <summary>
        /// private field that determines whether or not the <see cref="MasterPane" />
        /// <see cref="PaneBase.Rect" /> dimensions will be expanded to fill the
        /// available space when printing this <see cref="ZedGraphControl" />.
        /// </summary>
        /// <remarks>
        /// If <see cref="IsPrintKeepAspectRatio" /> is also true, then the <see cref="MasterPane" />
        /// <see cref="PaneBase.Rect" /> dimensions will be expanded to fit as large
        /// a space as possible while still honoring the visible aspect ratio.
        /// </remarks>
        bool _isPrintFillPage = true;
        /// <summary>
        /// private field that determines whether or not the visible aspect ratio of the
        /// <see cref="MasterPane" /> <see cref="PaneBase.Rect" /> will be preserved
        /// when printing this <see cref="ZedGraphControl" />.
        /// </summary>
        bool _isPrintKeepAspectRatio = true;
        /// <summary>
        /// private field that determines whether the settings of
        /// <see cref="ZedGraph.PaneBase.IsFontsScaled" /> and <see cref="PaneBase.IsPenWidthScaled" />
        /// will be overridden to true during printing operations.
        /// </summary>
        /// <remarks>
        /// Printing involves pixel maps that are typically of a dramatically different dimension
        /// than on-screen pixel maps.  Therefore, it becomes more important to scale the fonts and
        /// lines to give a printed image that looks like what is shown on-screen.  The default
        /// setting for <see cref="ZedGraph.PaneBase.IsFontsScaled" /> is true, but the default
        /// setting for <see cref="PaneBase.IsPenWidthScaled" /> is false.
        /// </remarks>
        /// <value>
        /// A value of true will cause both <see cref="ZedGraph.PaneBase.IsFontsScaled" /> and
        /// <see cref="PaneBase.IsPenWidthScaled" /> to be temporarily set to true during
        /// printing operations.
        /// </value>
        bool _isPrintScaleAll = true;
        /// <summary>
        /// private field that determines whether or not the context menu will be available.  Use the
        /// public property <see cref="IsShowContextMenu"/> to access this value.
        /// </summary>
        bool _isShowContextMenu = true;

        /// <summary>
        /// private field that determines whether or not a message box will be shown in response to
        /// a context menu "Copy" command.  Use the
        /// public property <see cref="IsShowCopyMessage"/> to access this value.
        /// </summary>
        /// <remarks>
        /// Note that, if this value is set to false, the user will receive no indicative feedback
        /// in response to a Copy action.
        /// </remarks>
        bool _isShowCopyMessage = true;
 

        bool _isShowHScrollBar;

        bool _isShowVScrollBar;
        //private bool		isScrollY2 = false;

        bool _isSynchronizeXAxes;
        bool _isSynchronizeYAxes;

        /// <summary>
        /// This private field contains the instance for the MasterPane object of this control.
        /// You can access the MasterPane object through the public property
        /// <see cref="ZedGraphControl.MasterPane"/>. This is nulled when this Control is
        /// disposed.
        /// </summary>
        MasterPane _masterPane;

        /// <summary>
        /// private field that stores a <see cref="PrintDocument" /> instance, which maintains
        /// a persistent selection of printer options.
        /// </summary>
        /// <remarks>
        /// This is needed so that a "Print" action utilizes the settings from a prior
        /// "Page Setup" action.</remarks>
        PrintDocument _pdSave;
        /// <summary>
        /// private field that determines the format for displaying tooltip date values.
        /// This format is passed to <see cref="XDate.ToString(string)"/>.
        /// Use the public property <see cref="PointDateFormat"/> to access this
        /// value.
        /// </summary>
        string _pointDateFormat = XDate.DefaultFormatStr;
        /// <summary>
        /// private field that determines the format for displaying tooltip values.
        /// This format is passed to <see cref="PointPairBase.ToString(string)"/>.
        /// Use the public property <see cref="PointValueFormat"/> to access this
        /// value.
        /// </summary>
        string _pointValueFormat = PointPairBase.DefaultFormat;
        SaveFileDialog _saveFileDialog = new SaveFileDialog();
        ScrollRange _xScrollRange;
        double _zoomStepFraction = 0.1;
        //private PrinterSettings printSave = null;
        //private PageSettings pageSave = null;
        #endregion
        #region Fields: Buttons & Keys Properties
        /// <summary>
        /// Gets or sets a value that determines which Mouse button will be used to edit point
        /// data values
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHEdit" /> and/or
        /// <see cref="IsEnableVEdit" /> are true.
        /// </remarks>
        /// <seealso cref="EditModifierKeys" />
        MouseButtons _editButtons = MouseButtons.Right;
        /// <summary>
        /// Gets or sets a value that determines which modifier keys will be used to edit point
        /// data values
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHEdit" /> and/or
        /// <see cref="IsEnableVEdit" /> are true.
        /// </remarks>
        /// <seealso cref="EditButtons" />
        Keys _editModifierKeys = Keys.Alt;
        /// <summary>
        /// Gets or sets a value that determines which Mouse button will be used to click on
        /// linkable objects
        /// </summary>
        /// <seealso cref="LinkModifierKeys" />
        MouseButtons _linkButtons = MouseButtons.Left;
        /// <summary>
        /// Gets or sets a value that determines which modifier keys will be used to click
        /// on linkable objects
        /// </summary>
        /// <seealso cref="LinkButtons" />
        Keys _linkModifierKeys = Keys.Alt;
        /// <summary>
        /// Gets or sets a value that determines which Mouse button will be used to perform
        /// panning operations
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHPan" /> and/or
        /// <see cref="IsEnableVPan" /> are true.  A Pan operation (dragging the graph with
        /// the mouse) should not be confused with a scroll operation (using a scroll bar to
        /// move the graph).
        /// </remarks>
        /// <seealso cref="PanModifierKeys" />
        /// <seealso cref="PanButtons2" />
        /// <seealso cref="PanModifierKeys2" />
        MouseButtons _panButtons = MouseButtons.Left;
        /// <summary>
        /// Gets or sets a value that determines which Mouse button will be used as a
        /// secondary option to perform panning operations
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHPan" /> and/or
        /// <see cref="IsEnableVPan" /> are true.  A Pan operation (dragging the graph with
        /// the mouse) should not be confused with a scroll operation (using a scroll bar to
        /// move the graph).
        /// </remarks>
        /// <seealso cref="PanModifierKeys2" />
        /// <seealso cref="PanButtons" />
        /// <seealso cref="PanModifierKeys" />
        MouseButtons _panButtons2 = MouseButtons.Middle;
        /// <summary>
        /// Gets or sets a value that determines which modifier keys will be used to perform
        /// panning operations
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHPan" /> and/or
        /// <see cref="IsEnableVPan" /> are true.  A Pan operation (dragging the graph with
        /// the mouse) should not be confused with a scroll operation (using a scroll bar to
        /// move the graph).
        /// </remarks>
        /// <seealso cref="PanButtons" />
        /// <seealso cref="PanButtons2" />
        /// <seealso cref="PanModifierKeys2" />
        Keys _panModifierKeys = Keys.Control;
        /// <summary>
        /// Gets or sets a value that determines which modifier keys will be used as a
        /// secondary option to perform panning operations
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHPan" /> and/or
        /// <see cref="IsEnableVPan" /> are true.  A Pan operation (dragging the graph with
        /// the mouse) should not be confused with a scroll operation (using a scroll bar to
        /// move the graph).
        /// </remarks>
        /// <seealso cref="PanButtons2" />
        /// <seealso cref="PanButtons" />
        /// <seealso cref="PanModifierKeys" />
        Keys _panModifierKeys2 = Keys.None;
        const Keys _selectAppendModifierKeys = Keys.Shift|Keys.Control;

        /// <summary>
        /// Gets or sets a value that determines which mouse button will be used to select
        /// <see cref="CurveItem" />'s.
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableSelection" /> is true.
        /// </remarks>
        /// <seealso cref="SelectModifierKeys" />
        MouseButtons _selectButtons = MouseButtons.Left;
        /// <summary>
        /// Gets or sets a value that determines which modifier keys will be used to select
        /// <see cref="CurveItem" />'s.
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableSelection" /> is true.
        /// </remarks>
        /// <seealso cref="SelectButtons" />
        Keys _selectModifierKeys = Keys.Shift;

        /// <summary>
        /// Gets or sets a value that determines which Mouse button will be used to perform
        /// zoom operations
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHZoom" /> and/or
        /// <see cref="IsEnableVZoom" /> are true.
        /// </remarks>
        /// <seealso cref="ZoomModifierKeys" />
        /// <seealso cref="ZoomButtons2" />
        /// <seealso cref="ZoomModifierKeys2" />
        MouseButtons _zoomButtons = MouseButtons.Left;

        /// <summary>
        /// Gets or sets a value that determines which Mouse button will be used as a
        /// secondary option to perform zoom operations
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHZoom" /> and/or
        /// <see cref="IsEnableVZoom" /> are true.
        /// </remarks>
        /// <seealso cref="ZoomModifierKeys2" />
        /// <seealso cref="ZoomButtons" />
        /// <seealso cref="ZoomModifierKeys" />
        MouseButtons _zoomButtons2 = MouseButtons.None;
        /// <summary>
        /// Gets or sets a value that determines which modifier keys will be used to perform
        /// zoom operations
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHZoom" /> and/or
        /// <see cref="IsEnableVZoom" /> are true.
        /// </remarks>
        /// <seealso cref="ZoomButtons" />
        /// <seealso cref="ZoomButtons2" />
        /// <seealso cref="ZoomModifierKeys2" />
        Keys _zoomModifierKeys = Keys.None;
        /// <summary>
        /// Gets or sets a value that determines which modifier keys will be used as a
        /// secondary option to perform zoom operations
        /// </summary>
        /// <remarks>
        /// This setting only applies if <see cref="IsEnableHZoom" /> and/or
        /// <see cref="IsEnableVZoom" /> are true.
        /// </remarks>
        /// <seealso cref="ZoomButtons" />
        /// <seealso cref="ZoomButtons2" />
        /// <seealso cref="ZoomModifierKeys2" />
        Keys _zoomModifierKeys2 = Keys.None;
        #endregion
        #region Fields: Temporary state variables
        readonly ZoomStateStack _zoomStateStack;
        CurveItem _dragCurve;
        Point _dragEndPt;

        int _dragIndex;
        /// <summary>
        /// Internal variable that stores the <see cref="GraphPane"/> reference for the Pane that is
        /// currently being zoomed or panned.
        /// </summary>
        GraphPane _dragPane;
        PointPair _dragStartPair;
        /// <summary>
        /// Internal variable that stores a rectangle which is either the zoom rectangle, or the incremental
        /// pan amount since the last mousemove event.
        /// </summary>
        Point _dragStartPt;
        /// <summary>
        /// Internal variable that indicates a point value is currently being edited.
        /// </summary>
        bool _isEditing;
        /// <summary>
        /// Internal variable that indicates the control is currently being panned.
        /// </summary>
        bool _isPanning;
        /// <summary>
        /// Internal variable that indicates the control is currently using selection. 
        /// </summary>
        bool _isSelecting;
        /// <summary>
        /// Internal variable that indicates the control is currently being zoomed. 
        /// </summary>
        bool _isZooming;

        //temporarily save the location of a context menu click so we can use it for reference
        // Note that Control.MousePosition ends up returning the position after the mouse has
        // moved to the menu item within the context menu.  Therefore, this point is saved so
        // that we have the point at which the context menu was first right-clicked
        internal Point _menuClickPt;
        /// <summary>
        /// private field that stores the state of the scale ranges prior to starting a panning action.
        /// </summary>
        ZoomState _zoomState;
        #endregion
        #region Constructors
        /// <summary>
        /// Default Constructor
        /// </summary>
        public ZedGraphControl() {
            InitializeComponent();

            // These commands do nothing, but they get rid of the compiler warnings for
            // unused events
            var b = MouseDown == null || MouseUp == null || MouseMove == null;

            // Link in these events from the base class, since we disable them from this class.
            base.MouseDown += ZedGraphControl_MouseDown;
            base.MouseUp += ZedGraphControl_MouseUp;
            base.MouseMove += ZedGraphControl_MouseMove;

            //this.MouseWheel += new System.Windows.Forms.MouseEventHandler( this.ZedGraphControl_MouseWheel );

            // Use double-buffering for flicker-free updating:
            SetStyle(
                ControlStyles.UserPaint|ControlStyles.AllPaintingInWmPaint
                    |ControlStyles.DoubleBuffer|ControlStyles.ResizeRedraw,
                true);
            //isTransparentBackground = false;
            //SetStyle( ControlStyles.Opaque, false );
            SetStyle(ControlStyles.SupportsTransparentBackColor, true);
            //this.BackColor = Color.Transparent;

            _resourceManager = new ResourceManager(
                "ZedGraph.ZedGraph.ZedGraphLocale",
                Assembly.GetExecutingAssembly());

            var rect = new Rectangle(0, 0, Size.Width, Size.Height);
            _masterPane = new MasterPane("", rect) {Margin = {All = 0}};
            _masterPane.Title.IsVisible = false;

            var titleStr = _resourceManager.GetString("title_def");
            var xStr = _resourceManager.GetString("x_title_def");
            var yStr = _resourceManager.GetString("y_title_def");

            //GraphPane graphPane = new GraphPane( rect, "Title", "X Axis", "Y Axis" );
            var graphPane = new GraphPane(rect, titleStr, xStr, yStr);
            using (var g = CreateGraphics()) graphPane.AxisChange(g);
                //g.Dispose();
            _masterPane.Add(graphPane);

            hScrollBar1.Minimum = 0;
            hScrollBar1.Maximum = 100;
            hScrollBar1.Value = 0;

            vScrollBar1.Minimum = 0;
            vScrollBar1.Maximum = 100;
            vScrollBar1.Value = 0;

            _xScrollRange = new ScrollRange(true);
            _yScrollRangeList = new ScrollRangeList();
            _y2ScrollRangeList = new ScrollRangeList();

            _yScrollRangeList.Add(new ScrollRange(true));
            _y2ScrollRangeList.Add(new ScrollRange(false));

            _zoomState = null;
            _zoomStateStack = new ZoomStateStack();
        }

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if the components should be
        /// disposed, false otherwise</param>
        protected override void Dispose(bool disposing) {
            lock (this) {
                if (disposing) if (components != null) components.Dispose();
                base.Dispose(disposing);

                _masterPane = null;
            }
        }
        #endregion
        #region Methods
        /// <summary>
        /// Called by the system to update the control on-screen
        /// </summary>
        /// <param name="e">
        /// A PaintEventArgs object containing the Graphics specifications
        /// for this Paint event.
        /// </param>
        protected override void OnPaint(PaintEventArgs e) {
            lock (this) {
                if (BeenDisposed || _masterPane == null || GraphPane == null) return;

                if (hScrollBar1 != null && GraphPane != null &&
                    vScrollBar1 != null && _yScrollRangeList != null) {
                    SetScroll(hScrollBar1, GraphPane.XAxis, _xScrollRange.Min, _xScrollRange.Max);
                    SetScroll(
                        vScrollBar1,
                        GraphPane.YAxis,
                        _yScrollRangeList[0].Min,
                        _yScrollRangeList[0].Max);
                }

                base.OnPaint(e);

                // Add a try/catch pair since the users of the control can't catch this one
                try {
                    _masterPane.Draw(e.Graphics);
                } catch (Exception ex) {
                    LogC.err("failed during draw with ", ex);
                }
            }

/*
			// first, see if an old thread is still running
			if ( t != null && t.IsAlive )
			{
				t.Abort();
			}

			//dt = new DrawingThread( e.Graphics, _masterPane );
			//g = e.Graphics;

			// Fire off the new thread
			t = new Thread( new ParameterizedThreadStart( DoDrawingThread ) );
			//ct.ApartmentState = ApartmentState.STA;
			//ct.SetApartmentState( ApartmentState.STA );
			DrawingThreadData dtd;
			dtd._g = e.Graphics;
			dtd._masterPane = _masterPane;

			t.Start( dtd );
			//ct.Join();
*/
        }

//		Thread t = null;
        //DrawingThread dt = null;

/*
		/// <summary>
		/// 
		/// </summary>
		/// <param name="dtdobj"></param>
		public void DoDrawingThread( object dtdobj )
		{
			try
			{
				DrawingThreadData dtd = (DrawingThreadData) dtdobj;

				if ( dtd._g != null && dtd._masterPane != null )
					dtd._masterPane.Draw( dtd._g );

				//				else
				//				{
				//					using ( Graphics g2 = CreateGraphics() )
				//						_masterPane.Draw( g2 );
				//				}
			}
			catch
			{

			}
		}
*/

        /// <summary>
        /// Called when the control has been resized.
        /// </summary>
        /// <param name="sender">
        /// A reference to the control that has been resized.
        /// </param>
        /// <param name="e">
        /// An EventArgs object.
        /// </param>
        protected void ZedGraphControl_ReSize(object sender, EventArgs e) {
            lock (this) {
                if (BeenDisposed || _masterPane == null) return;

                var newSize = Size;

                if (_isShowHScrollBar) {
                    hScrollBar1.Visible = true;
                    newSize.Height -= hScrollBar1.Size.Height;
                    hScrollBar1.Location = new Point(0, newSize.Height);
                    hScrollBar1.Size = new Size(newSize.Width, hScrollBar1.Height);
                } else hScrollBar1.Visible = false;

                if (_isShowVScrollBar) {
                    vScrollBar1.Visible = true;
                    newSize.Width -= vScrollBar1.Size.Width;
                    vScrollBar1.Location = new Point(newSize.Width, 0);
                    vScrollBar1.Size = new Size(vScrollBar1.Width, newSize.Height);
                } else vScrollBar1.Visible = false;

                using (var g = CreateGraphics()) _masterPane.ReSize(g, new RectangleF(0, 0, newSize.Width, newSize.Height));
                    //g.Dispose();
                Invalidate();
            }
        }

        /// <summary>This performs an axis change command on the graphPane.
        /// </summary>
        /// <remarks>
        /// This is the same as
        /// <c>ZedGraphControl.GraphPane.AxisChange( ZedGraphControl.CreateGraphics() )</c>, however,
        /// this method also calls <see cref="SetScrollRangeFromData" /> if <see cref="IsAutoScrollRange" />
        /// is true.
        /// </remarks>
        public virtual void AxisChange() {
            lock (this) {
                if (BeenDisposed || _masterPane == null) return;
                using (var g = CreateGraphics()) _masterPane.AxisChange(g);
                if (IsAutoScrollRange) SetScrollRangeFromData();
            }
        }
        #endregion
        #region Zoom States
        /// <summary>
        /// Save the current states of the GraphPanes to a separate collection.  Save a single
        /// (<see paramref="primaryPane" />) GraphPane if the panes are not synchronized
        /// (see <see cref="IsSynchronizeXAxes" /> and <see cref="IsSynchronizeYAxes" />),
        /// or save a list of states for all GraphPanes if the panes are synchronized.
        /// </summary>
        /// <param name="primaryPane">The primary GraphPane on which zoom/pan/scroll operations
        /// are taking place</param>
        /// <param name="type">The <see cref="ZoomState.StateType" /> that describes the
        /// current operation</param>
        /// <returns>The <see cref="ZoomState" /> that corresponds to the
        /// <see paramref="primaryPane" />.
        /// </returns>
        ZoomState ZoomStateSave(GraphPane primaryPane, ZoomState.StateType type) {
            ZoomStateClear();

            if (_isSynchronizeXAxes || _isSynchronizeYAxes)
                foreach (var pane in _masterPane._paneList) {
                    var state = new ZoomState(pane, type);
                    if (pane == primaryPane) _zoomState = state;
                    _zoomStateStack.Add(state);
                }
            else _zoomState = new ZoomState(primaryPane, type);

            return _zoomState;
        }

        /// <summary>
        /// Restore the states of the GraphPanes to a previously saved condition (via
        /// <see cref="ZoomStateSave" />.  This is essentially an "undo" for live
        /// pan and scroll actions.  Restores a single
        /// (<see paramref="primaryPane" />) GraphPane if the panes are not synchronized
        /// (see <see cref="IsSynchronizeXAxes" /> and <see cref="IsSynchronizeYAxes" />),
        /// or save a list of states for all GraphPanes if the panes are synchronized.
        /// </summary>
        /// <param name="primaryPane">The primary GraphPane on which zoom/pan/scroll operations
        /// are taking place</param>
        void ZoomStateRestore(GraphPane primaryPane) {
            if (_isSynchronizeXAxes || _isSynchronizeYAxes) {
                for (var i = 0; i < _masterPane._paneList.Count; i++) if (i < _zoomStateStack.Count) _zoomStateStack[i].ApplyState(_masterPane._paneList[i]);
            } else if (_zoomState != null) _zoomState.ApplyState(primaryPane);

            ZoomStateClear();
        }

        /// <summary>
        /// Place the previously saved states of the GraphPanes on the individual GraphPane
        /// <see cref="ZedGraph.GraphPane.ZoomStack" /> collections.  This provides for an
        /// option to undo the state change at a later time.  Save a single
        /// (<see paramref="primaryPane" />) GraphPane if the panes are not synchronized
        /// (see <see cref="IsSynchronizeXAxes" /> and <see cref="IsSynchronizeYAxes" />),
        /// or save a list of states for all GraphPanes if the panes are synchronized.
        /// </summary>
        /// <param name="primaryPane">The primary GraphPane on which zoom/pan/scroll operations
        /// are taking place</param>
        /// <returns>The <see cref="ZoomState" /> that corresponds to the
        /// <see paramref="primaryPane" />.
        /// </returns>
        void ZoomStatePush(GraphPane primaryPane) {
            if (_isSynchronizeXAxes || _isSynchronizeYAxes) {
                for (var i = 0; i < _masterPane._paneList.Count; i++) if (i < _zoomStateStack.Count) _masterPane._paneList[i].ZoomStack.Add(_zoomStateStack[i]);
            } else if (_zoomState != null) primaryPane.ZoomStack.Add(_zoomState);

            ZoomStateClear();
        }

        /// <summary>
        /// Clear the collection of saved states.
        /// </summary>
        void ZoomStateClear() {
            _zoomStateStack.Clear();
            _zoomState = null;
        }

        /// <summary>
        /// Clear all states from the undo stack for each GraphPane.
        /// </summary>
        void ZoomStatePurge() {
            foreach (var pane in _masterPane._paneList) pane.ZoomStack.Clear();
        }
        #endregion
    }
}