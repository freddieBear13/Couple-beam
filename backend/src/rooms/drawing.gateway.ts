import { 
    WebSocketGateway,
    OnGatewayConnection,
    OnGatewayDisconnect, 
    WebSocketServer,
    SubscribeMessage

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
    handleJoinRoom(client: Socket, roomId: string): void {
        client.join(roomId);
        this.logger.log(`Client ${client.id} joined room: ${roomId}`);
    }

    @SubscribeMessage('draw')
    handleDraw(client: Socket, payload: { roomId: string, points: any[] }): void {
        this.server.to(payload.roomId).emit('draw', payload.points);
    }

    @SubscribeMessage('undo')
    handleUndo(client: Socket, payload: { roomId: string, strokeIndex: number }): void {
        this.server.to(payload.roomId).emit('undo', payload.strokeIndex);
    }

    @SubscribeMessage('clear')
    handleClear(client: Socket, roomId: string): void {
        this.server.to(roomId).emit('clear');
    }
}