FROM node
MAINTAINER Vitaly Baum <vitaly.baum@gmail.com> 

EXPOSE 3000

RUN mkdir /app
ADD . /app
WORKDIR /app

RUN npm install

CMD node index.js
