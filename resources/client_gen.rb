arr = []

ARGF.each do |line|
  arr << line.chomp.split("\t")
end

res = arr.select { |x| x[5] } .group_by { |x| x[2].to_i + x[4].to_i - 1883 } 

puts "require 'socket';"

puts "\nclient = TCPSocket.new('127.0.0.1', 1883)\n"
puts "def hex_to_bin(s) s.scan(/../).map { |x| x.hex.chr }.join end"

res.each { |i, l| 

  puts "puts ''"
  puts "\nputs '#{i}'"
  puts "\nclient = TCPSocket.new('127.0.0.1', 1883)\n"

  l.each { |x| 
    protocol_name = [ "CONNECT", "CONNACK", "PUBLISH", "PUBACK", "PUBREC", "PUBREL", "PUBCOMP", "SUBSCRIBE", "SUBACK", "UNSUBSCRIBE", "UNSUBACK", "PINGREQ", "PINGRESP", "DISCONNECT" ].at((x[5][0..1].to_i(16) >> 4) - 1)

    if (x[2] != "1883")
      puts "\nclient.puts(hex_to_bin(\"#{x[5].gsub(':','')}\"))"
      puts "puts '#{protocol_name}'"
      puts "\nsleep(1)"
    end
  }
}
