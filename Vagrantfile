# -*- mode: ruby -*-
# vi: set ft=ruby :
Vagrant.configure(2) do |config|
  config.vm.box = "AntonioMeireles/coreos-stable"

  config.vm.synced_folder ".", "/home/core/JetMQ", id: "core", :nfs => true,  :mount_options   => ['nolock,vers=3,udp']

  config.vm.network "forwarded_port", guest: 80, host: 80
  config.vm.network "forwarded_port", guest: 1883, host: 1883
  config.vm.network "forwarded_port", guest: 5601, host: 5601

  config.vm.provision :shell, :inline => "
   if [ ! -f /opt/bin/docker-compose ]; then \
     sudo mkdir -p /opt/bin
     sudo curl -L https://github.com/docker/compose/releases/download/1.5.1/docker-compose-`uname -s`-`uname -m` > /opt/bin/docker-compose
     sudo chown root:root /opt/bin/docker-compose
     sudo chmod +x /opt/bin/docker-compose
   fi
  "
  config.vm.provision :shell, :inline => "cd JetMQ/ && docker-compose stop && docker-compose build", :run => "always"

end
