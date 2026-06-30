import { 
    WebSocketGateway,
    OnGatewayConnection,
    OnGatewayDisconnect, 
    WebSocketServer,
    SubscribeMessage,
    ConnectedSocket,
    MessageBody

} from "@nestjs/websockets";
import { Server, Socket } from "socket.io";
import { Logger } from "@nestjs/common";

@WebSocketGateway({ cors: true })
export class DrawingGateway implements OnGatewayConnection, OnGatewayDisconnect {
    @WebSocketServer()
    server: Server;

    private readonly logger = new Logger(DrawingGateway.name);

    handleConnection(client: Socket) {
        this.logger.log(`Client connected: ${client.id}`);
    }

    handleDisconnect(client: Socket) {
        this.logger.log(`Client disconnected: ${client.id}`);
    }

    @SubscribeMessage('joinRoom')
    handleJoinRoom(
        @ConnectedSocket() client: Socket, 
        @MessageBody() roomId: string
    ): void {
        client.join(roomId);
        this.logger.log(`Client ${client.id} joined room: ${roomId}`);
    }

    @SubscribeMessage('draw')
    handleDraw(
        @ConnectedSocket() client: Socket, 
        @MessageBody() payload: { roomId: string, points: any[] }
    ): void {
        client.broadcast.to(payload.roomId).emit('draw', payload.points);
    }

    @SubscribeMessage('undo')
    handleUndo(
        @ConnectedSocket() client: Socket, 
        @MessageBody() payload: { roomId: string }
    ): void {
        client.broadcast.to(payload.roomId).emit('undo');
    }

    @SubscribeMessage('clear')
    handleClear(
        @ConnectedSocket() client: Socket, 
        @MessageBody() roomId: string
    ): void {
        client.broadcast.to(roomId).emit('clear');
    }
}