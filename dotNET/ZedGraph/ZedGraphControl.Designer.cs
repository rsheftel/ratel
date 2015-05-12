using System.ComponentModel;
using System.Windows.Forms;

namespace ZedGraph {
    partial class ZedGraphControl {
        /// <summary> 
        /// Required designer variable.
        /// </summary>
        IContainer components;
        ContextMenuStrip contextMenuStrip1;
        HScrollBar hScrollBar1;
        ToolTip pointToolTip;
        VScrollBar vScrollBar1;
        #region Component Designer generated code
        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        void InitializeComponent() {
            components = new Container();
            vScrollBar1 = new VScrollBar();
            hScrollBar1 = new HScrollBar();
            pointToolTip = new ToolTip(components);
            contextMenuStrip1 = new ContextMenuStrip(components);
            SuspendLayout();
            // 
            // vScrollBar1
            // 
            vScrollBar1.Location = new System.Drawing.Point(128, 0);
            vScrollBar1.Name = "vScrollBar1";
            vScrollBar1.Size = new System.Drawing.Size(17, 128);
            vScrollBar1.TabIndex = 0;
            //this.vScrollBar1.MouseCaptureChanged += new System.EventHandler( this.ScrollBarMouseCaptureChanged );
            vScrollBar1.Scroll += vScrollBar1_Scroll;
            // 
            // hScrollBar1
            // 
            hScrollBar1.Location = new System.Drawing.Point(0, 128);
            hScrollBar1.Name = "hScrollBar1";
            hScrollBar1.Size = new System.Drawing.Size(128, 17);
            hScrollBar1.TabIndex = 1;
            //this.hScrollBar1.MouseCaptureChanged += new System.EventHandler( this.ScrollBarMouseCaptureChanged );
            hScrollBar1.Scroll += hScrollBar1_Scroll;
            // 
            // pointToolTip
            // 
            pointToolTip.AutoPopDelay = 5000;
            pointToolTip.InitialDelay = 100;
            pointToolTip.ReshowDelay = 0;
            // 
            // contextMenuStrip1
            // 
            contextMenuStrip1.Name = "contextMenuStrip1";
            contextMenuStrip1.Size = new System.Drawing.Size(61, 4);
            contextMenuStrip1.Opening += contextMenuStrip1_Opening;
            // 
            // ZedGraphControl
            // 
            AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            AutoScaleMode = AutoScaleMode.Font;
            ContextMenuStrip = contextMenuStrip1;
            Controls.Add(hScrollBar1);
            Controls.Add(vScrollBar1);
            Name = "ZedGraphControl";
            Resize += ZedGraphControl_ReSize;
            KeyUp += ZedGraphControl_KeyUp;
            KeyDown += ZedGraphControl_KeyDown;
            MouseWheel += ZedGraphControl_MouseWheel;
            ResumeLayout(false);
        }
        #endregion
    }
}