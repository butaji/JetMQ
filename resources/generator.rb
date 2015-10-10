arr = []

ARGF.each do |line|
  arr << line.chomp.split("\t")
end

res = arr.select { |x| x[5] } .group_by { |x| x[2].to_i + x[4].to_i - 1883 } 

res.each { |i, l| 

  puts "\"Scenario \##{i}\" in { \n\tval h = create_actor\n\n"

  l.each { |x| 
    if (x[2] != "1883")
      puts "\th ! \"#{x[5].gsub(':','')}\".toTcpReceived\n"
    else
      puts "\texpectMsg(\"#{x[5].gsub(':','')}\".toTcpWrite)\n\n"
    end
  }

  puts "\texpectNoMsg()\n\tsuccess\n}\n\n"

}
