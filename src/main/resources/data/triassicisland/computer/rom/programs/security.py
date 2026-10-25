# security - keycard readers and alarms.
#   security              show readers (with recent swipes) and alarms
#   security alarm on|off  sound or silence every alarm
#   security level <reader_id> <1-5>

devices = net.devices()
readers = [d for d in devices if d['type'] == 'keycard_reader']
alarms = [d for d in devices if d['type'] == 'alarm']

if args[:1] == ['alarm'] and len(args) == 2 and args[1] in ('on', 'off'):
    for d in alarms:
        net.call(d['id'], 'set_on', args[1] == 'on')
    print '%d alarm(s) %s.' % (len(alarms), 'sounding' if args[1] == 'on' else 'silenced')
elif args[:1] == ['level'] and len(args) == 3:
    net.call(args[1], 'set_required_level', int(args[2]))
    print '%s now needs level %s.' % (args[1], args[2])
else:
    print 'KEYCARD READERS'
    for d in readers:
        r = net.wrap(d['id'])
        print '  %-26s needs level %d' % (d['label'] or d['id'], r.required_level())
        for s in r.swipes()[:3]:
            print '      %-16s L%d %s' % (s['player'], s['level'], 'OK' if s['granted'] else 'DENIED')
    if not readers:
        print '  (none connected)'
    print 'ALARMS'
    for d in alarms:
        print '  %-26s %s' % (d['label'] or d['id'], 'SOUNDING' if net.call(d['id'], 'is_on') else 'quiet')
    if not alarms:
        print '  (none connected)'
