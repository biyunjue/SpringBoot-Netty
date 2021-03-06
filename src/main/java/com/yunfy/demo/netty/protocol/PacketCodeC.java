package com.yunfy.demo.netty.protocol;

import com.yunfy.demo.netty.protocol.request.*;
import com.yunfy.demo.netty.protocol.response.*;
import com.yunfy.demo.netty.serialize.Serializer;
import com.yunfy.demo.netty.serialize.impl.JSONSerializer;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import static com.yunfy.demo.netty.protocol.command.Command.*;

/**
 * @author yunfy
 * @create 2019-01-11 11:42
 **/
public class PacketCodeC {

    public static final int MAGIC_NUMBER = 0x12345678;
    public static final PacketCodeC INSTANCE = new PacketCodeC();
    /**
     * 存储所有的命令类型及对应的类
     **/
    private static final Map<Byte, Class<? extends Packet>> packetTypeMap;

    /**
     * 存储所有的序列化方式及对应的序列化类
     **/
    private static final Map<Byte, Serializer> serializerMap;

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
        packetTypeMap.put(LOGIN_RESPONSE, LoginResponsePacket.class);
        packetTypeMap.put(MESSAGE_REQUEST, MessageRequestPacket.class);
        packetTypeMap.put(MESSAGE_RESPONSE, MessageResponsePacket.class);
        packetTypeMap.put(LOGOUT_REQUEST, LogoutRequestPacket.class);
        packetTypeMap.put(LOGOUT_RESPONSE, LogoutResponsePacket.class);
        packetTypeMap.put(CREATE_GROUP_REQUEST, CreateGroupRequestPacket.class);
        packetTypeMap.put(CREATE_GROUP_RESPONSE, CreateGroupResponsePacket.class);
        packetTypeMap.put(JOIN_GROUP_REQUEST, JoinGroupRequestPacket.class);
        packetTypeMap.put(JOIN_GROUP_RESPONSE, JoinGroupResponsePacket.class);
        packetTypeMap.put(QUIT_GROUP_REQUEST, QuitGroupRequestPacket.class);
        packetTypeMap.put(QUIT_GROUP_RESPONSE, QuitGroupResponsePacket.class);
        packetTypeMap.put(LIST_GROUP_MEMBERS_REQUEST, ListGroupMembersRequestPacket.class);
        packetTypeMap.put(LIST_GROUP_MEMBERS_RESPONSE, ListGroupMembersResponsePacket.class);
        packetTypeMap.put(GROUP_MESSAGE_REQUEST, GroupMessageRequestPacket.class);
        packetTypeMap.put(GROUP_MESSAGE_RESPONSE, GroupMessageResponsePacket.class);

        serializerMap = new HashMap<>();
        Serializer serializer = new JSONSerializer();
        serializerMap.put(serializer.getSerializerAlgorithm(), serializer);
    }

    /**
     * 编码
     *
     * @param packet
     * @return
     */
    public void encode(ByteBuf byteBuf, Packet packet) {
        // 2. 序列化 java 对象
        byte[] bytes = Serializer.DEFAULT.serialize(packet);
        // 3. 实际编码过程
        // 魔数
        byteBuf.writeInt(MAGIC_NUMBER);
        // 协议版本
        byteBuf.writeByte(packet.getVersion());
        // 序列化方式
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        // 指令
        byteBuf.writeByte(packet.getCommand());
        // 数据长度
        byteBuf.writeInt(bytes.length);
        // 实际数据
        byteBuf.writeBytes(bytes);
    }

    /**
     * 解码
     *
     * @param byteBuf
     * @return
     */
    public Packet decode(ByteBuf byteBuf) {
        // 假定传过来的数据匹配需要的数据，跳过验证
        byteBuf.skipBytes(4);
        // 跳过版本号
        byteBuf.skipBytes(1);
        // 获取序列化方式
        byte serializeAlgorithm = byteBuf.readByte();
        // 获取指令
        byte command = byteBuf.readByte();
        // 数据包长度
        int length = byteBuf.readInt();

        //获取实际的数据
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializeAlgorithm);
        if (requestType != null && serializer != null) {
            return serializer.deserialize(requestType, bytes);
        }
        return null;
    }

    /**
     * 获取对应的序列化方式
     *
     * @param serializeAlgorithm
     * @return
     */
    private Serializer getSerializer(byte serializeAlgorithm) {

        return serializerMap.get(serializeAlgorithm);
    }

    /**
     * 获取对应的命令请求
     *
     * @param command
     * @return
     */
    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }
}
