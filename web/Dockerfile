FROM node
MAINTAINER Vitaly Baum <vitaly.baum@gmail.com> 

EXPOSE 3000

RUN mkdir /app
ADD ./package.json /app/package.json
WORKDIR /app
RUN npm install
# cache packages if wasnt changed

ADD . /app

CMD node index.js
