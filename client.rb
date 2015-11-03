require 'socket';

client = TCPSocket.new('127.0.0.1', 1883)
def hex_to_bin(s) s.scan(/../).map { |x| x.hex.chr }.join end

#50856

client = TCPSocket.new('127.0.0.1', 1883)

client.puts(hex_to_bin("101600044d51545404020000000a6d79636c69656e746964"))
puts 'CONNECT'

sleep(1)

client.puts(hex_to_bin("e000"))
puts 'DISCONNECT'

sleep(1)

#50857

client = TCPSocket.new('127.0.0.1', 1883)

client.puts(hex_to_bin("101700044d51545404020000000b6d79636c69656e74696432"))
puts 'CONNECT'

sleep(1)

client.puts(hex_to_bin("e000"))
puts 'DISCONNECT'

sleep(1)

#50858

client = TCPSocket.new('127.0.0.1', 1883)

client.puts(hex_to_bin("101a00044d51545404020000000e636c65616e2072657461696e6564"))
puts 'CONNECT'

sleep(1)

client.puts(hex_to_bin("8206000200012300"))
puts 'SUBSCRIBE'

sleep(1)

client.puts(hex_to_bin("e000"))
puts 'DISCONNECT'

sleep(1)

#50859

client = TCPSocket.new('127.0.0.1', 1883)

client.puts(hex_to_bin("101600044d51545404020000000a6d79636c69656e746964"))
puts 'CONNECT'

sleep(1)

client.puts(hex_to_bin("e000"))
puts 'DISCONNECT'

sleep(1)

#50860

client = TCPSocket.new('127.0.0.1', 1883)

client.puts(hex_to_bin("101600044d51545404020000000a6d79636c69656e746964"))
puts 'CONNECT'

sleep(1)

client.puts(hex_to_bin("820b00020006546f7069634102"))
puts 'SUBSCRIBE'

sleep(1)

client.puts(hex_to_bin("300d0006546f70696341716f732030"))
puts 'PUBLISH'

sleep(1)

client.puts(hex_to_bin("320f0006546f706963410003716f732031"))
puts 'PUBLISH'

sleep(1)

client.puts(hex_to_bin("340f0006546f706963410004716f732032"))
puts 'PUBLISH'

sleep(1)

client.puts(hex_to_bin("40020001"))
puts 'PUBACK'

sleep(1)

client.puts(hex_to_bin("62020004"))
puts 'PUBREL'

sleep(1)

client.puts(hex_to_bin("50020002"))
puts 'PUBREC'

sleep(1)

client.puts(hex_to_bin("70020002"))
puts 'PUBCOMP'

sleep(1)

client.puts(hex_to_bin("e000"))
puts 'DISCONNECT'

sleep(1)

#50861

client = TCPSocket.new('127.0.0.1', 1883)

client.puts(hex_to_bin("101600044d51545404020000000a6d79636c69656e746964"))
puts 'CONNECT'

sleep(1)

client.puts(hex_to_bin("101600044d51545404020000000a6d79636c69656e746964"))
puts 'CONNECT'

sleep(1)

#50862

client = TCPSocket.new('127.0.0.1', 1883)

client.puts(hex_to_bin("10140002686a04020000000a6d79636c69656e746964"))
puts 'CONNECT'

sleep(1)
