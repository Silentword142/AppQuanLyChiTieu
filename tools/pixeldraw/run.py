import sys, importlib
from engine import sheet
mods = sys.argv[2:] or ['icons_a']
icons = {}
for m in mods: icons.update({k: f().render() for k, f in importlib.import_module(m).I.items()})
sheet(icons, sys.argv[1])
