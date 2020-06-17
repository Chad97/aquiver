package org.aquiver.mvc.render;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.aquiver.RequestContext;
import org.aquiver.mvc.Route;
import org.aquiver.mvc.view.ViewType;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.aquiver.mvc.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author WangYi
 * @since 2020/6/17
 */
public class JSONResponseRender implements ResponseRender {

  @Override
  public boolean support(ViewType viewType) {
    return viewType.equals(ViewType.JSON);
  }

  @Override
  public void render(Route route, RequestContext requestContext) {
    FullHttpRequest httpRequest = requestContext.getHttpRequest();
    Object result = route.getInvokeResult();
    ByteBuf byteBuf = Unpooled.copiedBuffer(Objects.isNull(result) ? "".getBytes(CharsetUtil.UTF_8)
            : JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8));

    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
            HTTP_1_1, httpRequest.decoderResult().isSuccess() ? OK : BAD_REQUEST, byteBuf);

    HttpHeaders headers = fullHttpResponse.headers();
    headers.set(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON_VALUE);
    if (HttpUtil.isKeepAlive(httpRequest)) {
      headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      headers.setInt(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
    }
    requestContext.getContext().writeAndFlush(fullHttpResponse);
  }
}
