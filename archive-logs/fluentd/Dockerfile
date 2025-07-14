FROM fluent/fluentd:v1.16-debian

USER root

RUN gem install fluent-plugin-s3 --no-document

RUN mkdir -p /fluentd/buffer /fluentd/log