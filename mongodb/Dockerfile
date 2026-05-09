FROM mongo:6
LABEL maintainer="Alexander Lukyanchikov <sqshq@sqshq.com>"

COPY init.sh /init.sh
COPY ./dump /

# hadolint ignore=DL3008
RUN apt-get update \
 && apt-get install -y --no-install-recommends dos2unix psmisc \
 && apt-get autoremove -y \
 && apt-get clean \
 && rm -rf /var/cache/* /var/lib/apt/lists/* \
 && chmod +x /init.sh \
 && dos2unix -n /init.sh /initx.sh \
 && chmod +x /initx.sh

ENTRYPOINT ["/initx.sh"]
