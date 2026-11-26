import sys, importlib
from px64 import sheet
icons = {}
for m in sys.argv[2:] or ['icons64_a']: icons.update({k: f().render() for k, f in importlib.import_module(m).I.items()})
sheet(icons, sys.argv[1])
