FROM node
MAINTAINER Vitaly Baum <vitaly.baum@gmail.com> 

EXPOSE 3000

RUN git clone https://github.com/butaji/JetMQ

WORKDIR JetMQ/www

RUN npm install

CMD node index.js | app.log
