FROM ubuntu:latest
LABEL authors="akira"

ENTRYPOINT ["top", "-b"]