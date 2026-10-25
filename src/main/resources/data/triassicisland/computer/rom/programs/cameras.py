# cameras - look through the security cameras on this network.
#   cameras        pick from a list
#   cameras <n>    jump straight to camera n
# In the camera view: A/D pan, W/S tilt, Left/Right switch camera, Esc to come back.

cams = [d for d in net.devices() if d['type'] == 'security_camera']
if not cams:
    print 'No cameras on the network. Connect some with data cables.'
else:
    choice = args[0] if args else None
    if choice is None:
        for i, d in enumerate(cams):
            status = 'ok' if net.call(d['id'], 'is_powered') else 'NO POWER'
            print '%2d  %-30s %s' % (i + 1, d['label'] or d['id'], status)
        choice = raw_input('Camera number (Enter = 1): ').strip() or '1'
    try:
        n = int(choice)
    except ValueError:
        n = 0
    if 1 <= n <= len(cams):
        term.view_camera(cams[n - 1]['id'])
    else:
        print 'Pick a number from 1 to %d.' % len(cams)
