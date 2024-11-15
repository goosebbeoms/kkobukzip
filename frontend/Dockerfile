# Node.js를 사용한 빌드 스테이지
FROM node:latest AS build
WORKDIR /frontend/
COPY package*.json ./
RUN npm install
COPY . .

ENV VITE_BASE_URL=https://j11c107.p.ssafy.io/api
ENV VITE_SSE_MAIN_URL=https://j11c107.p.ssafy.io/api/main/notifications/sse/subscribe
ENV VITE_SSE_AUCTION_URL=https://j11c107.p.ssafy.io/api/auction/sse/subscribe
ENV VITE_SOCKET_MAIN_URL=wss://j11c107.p.ssafy.io/ws/main
ENV VITE_SOCKET_AUCTION_URL=wss://j11c107.p.ssafy.io/ws/auction

RUN npm run build

# Nginx를 사용한 배포 스테이지
FROM nginx:latest
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/nginx.conf
# 빌드된 결과물을 Nginx의 루트 디렉토리로 복사
COPY --from=build /frontend/dist /usr/share/nginx/html

# 서버 실행
CMD [ "nginx", "-g", "daemon off;" ]