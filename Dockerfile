FROM nginx
RUN apt-get update && apt-get install -y procps
WORKDIR /usr/share/nginx/html
COPY web/Hello_docker.html /usr/share/nginx/html
CMD cd /usr/share/nginx/html && sed -e s/Docker/"$AUTHOR"/ Hello_docker.html > index.html ; nginx -g 'daemon off;'
