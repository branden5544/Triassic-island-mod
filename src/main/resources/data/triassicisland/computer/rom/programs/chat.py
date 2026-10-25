# chat - messages between computers on the same network.
#   chat <computer_id> <message>   send to one computer
#   chat all <message>             send to every computer
#   chat listen                    print messages as they arrive (Ctrl+T to stop)

if not args:
    print 'Usage: chat <id|all> <message>  or  chat listen'
    print 'This computer is ' + computer.id()
elif args[0] == 'listen':
    print 'Listening as %s... (Ctrl+T to stop)' % computer.id()
    while True:
        message = net.receive()
        print '[%s] %s' % message
elif len(args) >= 2:
    text = ' '.join(args[1:])
    if args[0] == 'all':
        print 'Sent to %d computer(s).' % net.broadcast(text)
    elif net.send(args[0], text):
        print 'Sent.'
    else:
        print 'No computer %s on this network.' % args[0]
