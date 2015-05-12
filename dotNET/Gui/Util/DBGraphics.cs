using System.Drawing;
using Q.Util;

namespace Gui.Util {
    /// <summary>
    /// Class to implement Double Buffering 
    /// NT Almond 
    /// 24 July 2003
    /// </summary>
    /// 
    public class DBGraphics
    {
        private Bitmap		memoryBitmap;
        private	int			width;
        private	int			height;
        int count;

        /// <summary>
        /// Default constructor
        /// </summary>
        public DBGraphics()
        {
            width	= 0;
            height	= 0;
        }

        /// <summary>
        /// Creates double buffer object
        /// </summary>
        /// <param name="unused">Window forms Graphics Object</param>
        /// <param name="newWidth">width of paint area</param>
        /// <param name="newHeight">height of paint area</param>
        /// <returns>true/false if double buffer is created</returns>
        public bool CreateDoubleBuffer(Graphics unused, int newWidth, int newHeight)
        {

            if (memoryBitmap != null)
            {
                memoryBitmap.Dispose();
                memoryBitmap = null;
            }

            if (g != null)
            {
                g.Dispose();
                g = null;
            }

            if (newWidth == 0 || newHeight == 0)
                return false;


            if ((newWidth != width) || (newHeight != height))
            {
                width = newWidth;
                height = newHeight;

                memoryBitmap	= new Bitmap(newWidth, newHeight);
                g		= Graphics.FromImage(memoryBitmap);
            }

            return true;
        }


        /// <summary>
        /// Renders the double buffer to the screen
        /// </summary>
        /// <param name="realGraphics">Window forms Graphics Object</param>
        public void Render(Graphics realGraphics) {
            if (memoryBitmap == null) return;
            realGraphics.DrawImage(memoryBitmap, new Rectangle(0, 0, width, height), 0, 0, width, height, GraphicsUnit.Pixel);
            var filename = @"C:\logs\chart." + (count++) + ".bmp";
            //memoryBitmap.Save(filename);
            LogC.debug("writing file " + filename);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns>true if double buffering can be achieved</returns>
        public bool CanDoubleBuffer()
        {
            return g != null;
        }

        /// <summary>
        /// Accessor for memory graphics object
        /// </summary>
        public Graphics g { get; set; }
    }
}