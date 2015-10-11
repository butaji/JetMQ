arr = []

ARGF.each do |line|
  arr << line.chomp.split("\t")
end

res = arr.select { |x| x[5] } .group_by { |x| x[2].to_i + x[4].to_i - 1883 } 

res.each { |i, l| 

  puts "\"Scenario \##{i}\" in { \n\tval h = create_actor\n\n"

  l.each { |x| 
    protocol_name = [ "CONNECT", "CONNACK", "PUBLISH", "PUBACK", "PUBREC", "PUBREL", "PUBCOMP", "SUBSCRIBE", "SUBACK", "UNSUBSCRIBE", "UNSUBACK", "PINGREQ", "PINGRESP", "DISCONNECT" ].at((x[5][0..1].to_i(16) >> 4) - 1)

    if (x[2] != "1883")
      puts "\th ! \"#{x[5].gsub(':','')}\".toTcpReceived //#{protocol_name}\n"
    else
      puts "\texpectMsg(\"#{x[5].gsub(':','')}\".toTcpWrite) //#{protocol_name}\n\n"
    end
  }

  puts "\texpectNoMsg()\n\tsuccess\n}\n\n"

}
