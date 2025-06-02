# syntax=docker/dockerfile:1.4
FROM node:20-alpine AS runtime

WORKDIR /app

COPY .next/standalone ./
COPY .next/static ./.next/static
COPY public ./public

EXPOSE 3000
CMD ["node", "server.js"]