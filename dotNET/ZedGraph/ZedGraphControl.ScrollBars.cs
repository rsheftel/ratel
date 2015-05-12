using System;
using System.Windows.Forms;

namespace ZedGraph {
    partial class ZedGraphControl {
        #region ScrollBars
        void vScrollBar1_Scroll(object sender, ScrollEventArgs e) {
            if (GraphPane == null) return;
            if ((e.Type != ScrollEventType.ThumbPosition &&
                e.Type != ScrollEventType.ThumbTrack) ||
                    (e.Type == ScrollEventType.ThumbTrack &&
                        _zoomState == null)) ZoomStateSave(GraphPane, ZoomState.StateType.Scroll);
            for (var i = 0; i < GraphPane.YAxisList.Count; i++) {
                var scroll = _yScrollRangeList[i];
                if (!scroll.IsScrollable) continue;
                Axis axis = GraphPane.YAxisList[i];
                HandleScroll(
                    axis,
                    e.NewValue,
                    scroll.Min,
                    scroll.Max,
                    vScrollBar1.LargeChange,
                    !axis.Scale.IsReverse);
            }

            for (var i = 0; i < GraphPane.Y2AxisList.Count; i++) {
                var scroll = _y2ScrollRangeList[i];
                if (!scroll.IsScrollable) continue;
                Axis axis = GraphPane.Y2AxisList[i];
                HandleScroll(
                    axis,
                    e.NewValue,
                    scroll.Min,
                    scroll.Max,
                    vScrollBar1.LargeChange,
                    !axis.Scale.IsReverse);
            }

            ApplyToAllPanes(GraphPane);

            ProcessEventStuff(vScrollBar1, e);
        }

        void ApplyToAllPanes(GraphPane primaryPane) {
            foreach (var pane in _masterPane._paneList)
                if (pane != primaryPane) {
                    if (_isSynchronizeXAxes) Synchronize(primaryPane.XAxis, pane.XAxis);
                    if (_isSynchronizeYAxes) Synchronize(primaryPane.YAxis, pane.YAxis);
                }
        }

        static void Synchronize(Axis source, Axis dest) {
            dest._scale._min = source._scale._min;
            dest._scale._max = source._scale._max;
            dest._scale._majorStep = source._scale._majorStep;
            dest._scale._minorStep = source._scale._minorStep;
            dest._scale._minAuto = source._scale._minAuto;
            dest._scale._maxAuto = source._scale._maxAuto;
            dest._scale._majorStepAuto = source._scale._majorStepAuto;
            dest._scale._minorStepAuto = source._scale._minorStepAuto;
        }

        void hScrollBar1_Scroll(object sender, ScrollEventArgs e) {
            if (GraphPane == null) return;
            if ((e.Type != ScrollEventType.ThumbPosition &&
                e.Type != ScrollEventType.ThumbTrack) ||
                    (e.Type == ScrollEventType.ThumbTrack &&
                        _zoomState == null)) ZoomStateSave(GraphPane, ZoomState.StateType.Scroll);

            HandleScroll(
                GraphPane.XAxis,
                e.NewValue,
                _xScrollRange.Min,
                _xScrollRange.Max,
                hScrollBar1.LargeChange,
                GraphPane.XAxis.Scale.IsReverse);

            ApplyToAllPanes(GraphPane);

            ProcessEventStuff(hScrollBar1, e);
        }

        void ProcessEventStuff(ScrollBar scrollBar, ScrollEventArgs e) {
            if (e.Type == ScrollEventType.ThumbTrack) {
                if (ScrollProgressEvent != null)
                    ScrollProgressEvent(
                        this,
                        hScrollBar1,
                        _zoomState,
                        new ZoomState(GraphPane, ZoomState.StateType.Scroll));
            } else // if ( e.Type == ScrollEventType.ThumbPosition )
                if (_zoomState != null && _zoomState.IsChanged(GraphPane)) {
                    //this.GraphPane.ZoomStack.Push( _zoomState );
                    ZoomStatePush(GraphPane);

                    // Provide Callback to notify the user of pan events
                    if (ScrollDoneEvent != null)
                        ScrollDoneEvent(
                            this,
                            hScrollBar1,
                            _zoomState,
                            new ZoomState(GraphPane, ZoomState.StateType.Scroll));

                    _zoomState = null;
                }

            if (ScrollEvent != null) ScrollEvent(scrollBar, e);
        }

/*
		/// <summary>
		/// Use the MouseCaptureChanged as an indicator for the start and end of a scrolling operation
		/// </summary>
		private void ScrollBarMouseCaptureChanged( object sender, EventArgs e )
		{
			return;

			ScrollBar scrollBar = sender as ScrollBar;
			if ( scrollBar != null )
			{
				// If this is the start of a new scroll, then Capture will be true
				if ( scrollBar.Capture )
				{
					// save the original zoomstate
					//_zoomState = new ZoomState( this.GraphPane, ZoomState.StateType.Scroll );
					ZoomStateSave( this.GraphPane, ZoomState.StateType.Scroll );
				}
				else
				{
					// push the prior saved zoomstate, since the scale ranges have already been changed on
					// the fly during the scrolling operation
					if ( _zoomState != null && _zoomState.IsChanged( this.GraphPane ) )
					{
						//this.GraphPane.ZoomStack.Push( _zoomState );
						ZoomStatePush( this.GraphPane );

						// Provide Callback to notify the user of pan events
						if ( this.ScrollDoneEvent != null )
							this.ScrollDoneEvent( this, scrollBar, _zoomState,
										new ZoomState( this.GraphPane, ZoomState.StateType.Scroll ) );

						_zoomState = null;
					}
				}
			}
		}
*/

        void HandleScroll(
            Axis axis,
            int newValue,
            double scrollMin,
            double scrollMax,
            int largeChange,
            bool reverse) {
            if (axis == null) return;
            if (scrollMin > axis._scale._min) scrollMin = axis._scale._min;
            if (scrollMax < axis._scale._max) scrollMax = axis._scale._max;

            var span = _ScrollControlSpan - largeChange;
            if (span <= 0) return;

            if (reverse) newValue = span - newValue;

            var scale = axis._scale;

            var delta = scale._maxLinearized - scale._minLinearized;
            var scrollMin2 = scale.Linearize(scrollMax) - delta;
            scrollMin = scale.Linearize(scrollMin);
            //scrollMax = scale.Linearize( scrollMax );
            var val = scrollMin + newValue / (double) span *
                (scrollMin2 - scrollMin);
            scale._minLinearized = val;
            scale._maxLinearized = val + delta;
            /*
								if ( axis.Scale.IsLog )
								{
									double ratio = axis._scale._max / axis._scale._min;
									double scrollMin2 = scrollMax / ratio;

									double val = scrollMin * Math.Exp( (double)newValue / (double)span *
												( Math.Log( scrollMin2 ) - Math.Log( scrollMin ) ) );
									axis._scale._min = val;
									axis._scale._max = val * ratio;
								}
								else
								{
									double delta = axis._scale._max - axis._scale._min;
									double scrollMin2 = scrollMax - delta;

									double val = scrollMin + (double)newValue / (double)span *
												( scrollMin2 - scrollMin );
									axis._scale._min = val;
									axis._scale._max = val + delta;
								}
				*/
            Invalidate();
        }

        /// <summary>
        /// Sets the value of the scroll range properties (see <see cref="ScrollMinX" />,
        /// <see cref="ScrollMaxX" />, <see cref="YScrollRangeList" />, and 
        /// <see cref="Y2ScrollRangeList" /> based on the actual range of the data for
        /// each corresponding <see cref="Axis" />.
        /// </summary>
        /// <remarks>
        /// This method is called automatically by <see cref="AxisChange" /> if
        /// <see cref="IsAutoScrollRange" />
        /// is true.  Note that this will not be called if you call AxisChange directly from the
        /// <see cref="GraphPane" />.  For example, zedGraphControl1.AxisChange() works properly, but
        /// zedGraphControl1.GraphPane.AxisChange() does not.</remarks>
        public void SetScrollRangeFromData() {
            if (GraphPane == null) return;
            var grace = CalcScrollGrace(
                GraphPane.XAxis.Scale._rangeMin,
                GraphPane.XAxis.Scale._rangeMax);

            _xScrollRange.Min = GraphPane.XAxis.Scale._rangeMin - grace;
            _xScrollRange.Max = GraphPane.XAxis.Scale._rangeMax + grace;
            _xScrollRange.IsScrollable = true;

            for (var i = 0; i < GraphPane.YAxisList.Count; i++) {
                Axis axis = GraphPane.YAxisList[i];
                grace = CalcScrollGrace(axis.Scale._rangeMin, axis.Scale._rangeMax);
                var range = new ScrollRange(
                    axis.Scale._rangeMin - grace,
                    axis.Scale._rangeMax + grace,
                    _yScrollRangeList[i].IsScrollable);

                if (i >= _yScrollRangeList.Count) _yScrollRangeList.Add(range);
                else _yScrollRangeList[i] = range;
            }

            for (var i = 0; i < GraphPane.Y2AxisList.Count; i++) {
                Axis axis = GraphPane.Y2AxisList[i];
                grace = CalcScrollGrace(axis.Scale._rangeMin, axis.Scale._rangeMax);
                var range = new ScrollRange(
                    axis.Scale._rangeMin - grace,
                    axis.Scale._rangeMax + grace,
                    _y2ScrollRangeList[i].IsScrollable);

                if (i >= _y2ScrollRangeList.Count) _y2ScrollRangeList.Add(range);
                else _y2ScrollRangeList[i] = range;
            }

            //this.GraphPane.CurveList.GetRange( out scrollMinX, out scrollMaxX,
            //		out scrollMinY, out scrollMaxY, out scrollMinY2, out scrollMaxY2, false, false,
            //		this.GraphPane );
        }

        double CalcScrollGrace(double min, double max) {
            if (Math.Abs(max - min) < 1e-30)
                if (Math.Abs(max) < 1e-30) return ScrollGrace;
                else return max * ScrollGrace;
            return (max - min) * ScrollGrace;
        }

        static void SetScroll(ScrollBar scrollBar, Axis axis, double scrollMin, double scrollMax) {
            if (scrollBar == null || axis == null) return;
            scrollBar.Minimum = 0;
            scrollBar.Maximum = _ScrollControlSpan - 1;

            if (scrollMin > axis._scale._min) scrollMin = axis._scale._min;
            if (scrollMax < axis._scale._max) scrollMax = axis._scale._max;

            var scale = axis._scale;
            var minLinearized = scale._minLinearized;
            var maxLinearized = scale._maxLinearized;
            scrollMin = scale.Linearize(scrollMin);
            scrollMax = scale.Linearize(scrollMax);

            var scrollMin2 = scrollMax - (maxLinearized - minLinearized);
            /*
				if ( axis.Scale.IsLog )
					scrollMin2 = scrollMax / ( axis._scale._max / axis._scale._min );
				else
					scrollMin2 = scrollMax - ( axis._scale._max - axis._scale._min );
				*/
            if (scrollMin >= scrollMin2) {
                //scrollBar.Visible = false;
                scrollBar.Enabled = false;
                scrollBar.Value = 0;
            } else {
                var ratio = (maxLinearized - minLinearized) / (scrollMax - scrollMin);

                /*
					if ( axis.Scale.IsLog )
						ratio = ( Math.Log( axis._scale._max ) - Math.Log( axis._scale._min ) ) /
									( Math.Log( scrollMax ) - Math.Log( scrollMin ) );
					else
						ratio = ( axis._scale._max - axis._scale._min ) / ( scrollMax - scrollMin );
					*/

                var largeChange = (int) (ratio * _ScrollControlSpan + 0.5);
                if (largeChange < 1) largeChange = 1;
                scrollBar.LargeChange = largeChange;

                var smallChange = largeChange / _ScrollSmallRatio;
                if (smallChange < 1) smallChange = 1;
                scrollBar.SmallChange = smallChange;

                var span = _ScrollControlSpan - largeChange;

                var val = (int) ((minLinearized - scrollMin) / (scrollMin2 - scrollMin) *
                    span + 0.5);
                /*
					if ( axis.Scale.IsLog )
						val = (int)( ( Math.Log( axis._scale._min ) - Math.Log( scrollMin ) ) /
								( Math.Log( scrollMin2 ) - Math.Log( scrollMin ) ) * span + 0.5 );
					else
						val = (int)( ( axis._scale._min - scrollMin ) / ( scrollMin2 - scrollMin ) *
								span + 0.5 );
					*/
                if (val < 0) val = 0;
                else if (val > span) val = span;

                //if ( ( axis is XAxis && axis.IsReverse ) || ( ( ! axis is XAxis ) && ! axis.IsReverse ) )
                if ((axis is XAxis) == axis.Scale.IsReverse) val = span - val;

                if (val < scrollBar.Minimum) val = scrollBar.Minimum;
                if (val > scrollBar.Maximum) val = scrollBar.Maximum;

                scrollBar.Value = val;
                scrollBar.Enabled = true;
                //scrollBar.Visible = true;
            }
        }
        #endregion
    }
}