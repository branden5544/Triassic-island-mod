# doors - open and close fence gates.
#   doors                  list gates
#   doors open <id|all>
#   doors close <id|all>

gates = [d['id'] for d in net.devices() if d['type'] == 'electric_fence_gate']

if len(args) < 2 or args[0] not in ('open', 'close'):
    for gate_id in gates:
        print '%-28s %s' % (gate_id, 'open' if net.call(gate_id, 'is_open') else 'closed')
    if not gates:
        print 'No gates on the network.'
    print 'Usage: doors open|close <id|all>'
else:
    targets = gates if args[1] == 'all' else [args[1]]
    for gate_id in targets:
        net.call(gate_id, args[0])
        print '%s %s' % (gate_id, 'opened' if args[0] == 'open' else 'closed')
