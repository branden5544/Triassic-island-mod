# devices - list everything connected to this computer by data cables.

found = net.devices()
if not found:
    print 'No devices. Connect machines, breakers or gates'
    print 'with data cables (or combined cables).'
for d in found:
    print '%-28s %s' % (d['id'], d['label'])
print '%d device(s).  Try: net.methods(id) in python' % len(found)
