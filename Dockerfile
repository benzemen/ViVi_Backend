FROM ubuntu:latest
LABEL authors="benzeman"

ENTRYPOINT ["top", "-b"]