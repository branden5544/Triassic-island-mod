# smart - smart switches and sliding doors (wired, or wireless through a WiFi router on this network).
#   smart                          list them
#   smart on|off|toggle <id|all>   switch smart switches
#   smart door <id|all> auto|open|locked

devices = net.devices()
switches = [d['id'] for d in devices if d['type'] == 'smart_switch']
doors = [d['id'] for d in devices if d['type'] == 'sliding_glass_door']

if args[:1] in (['on'], ['off'], ['toggle']) and len(args) == 2:
    method = {'on': 'turn_on', 'off': 'turn_off', 'toggle': 'toggle'}[args[0]]
    for s in (switches if args[1] == 'all' else [args[1]]):
        net.call(s, method)
        print('%s is now %s' % (s, 'ON' if net.call(s, 'is_on') else 'OFF'))
elif args[:1] == ['door'] and len(args) == 3:
    for d in (doors if args[1] == 'all' else [args[1]]):
        net.call(d, 'set_mode', args[2])
        print('%s: %s' % (d, args[2]))
else:
    print('SMART SWITCHES')
    for s in switches:
        print('  %-28s %s' % (s, 'ON' if net.call(s, 'is_on') else 'OFF'))
    if not switches:
        print('  (none - connect one, or a WiFi router near them)')
    print('SLIDING DOORS')
    for d in doors:
        print('  %-28s %-7s %s' % (d, net.call(d, 'mode'), 'open' if net.call(d, 'is_open') else 'closed'))
    if not doors:
        print('  (none)')
    print('Usage: smart on|off|toggle <id|all>, smart door <id|all> auto|open|locked')
