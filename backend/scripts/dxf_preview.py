#!/usr/bin/env python3
import sys
import ezdxf
from ezdxf.addons.drawing import RenderContext, Frontend
from ezdxf.addons.drawing.svg import SVGBackend

def convert_to_svg(filepath):
    try:
        # Read the DXF file
        doc = ezdxf.readfile(filepath)
        msp = doc.modelspace()
        
        # Prepare the rendering context and backend
        ctx = RenderContext(doc)
        backend = SVGBackend()
        frontend = Frontend(ctx, backend)
        
        # Draw the model space
        frontend.draw_layout(msp)
        
        # Output the SVG content
        return backend.get_string()
    except Exception as e:
        sys.stderr.write(f"Error converting DXF to SVG: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.stderr.write("Usage: python3 dxf_preview.py <path_to_dxf_file>")
        sys.exit(1)
        
    print(convert_to_svg(sys.argv[1]))
