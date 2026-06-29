import { Injectable, NotFoundException, ConflictException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Room } from './room.entity';
import { Stroke } from './stroke.entity';
import { SaveStrokeDTO } from './save-stroke.dto';

@Injectable()
export class RoomsService {
    constructor(
        @InjectRepository(Room)
        private readonly roomRepository: Repository<Room>,
        @InjectRepository(Stroke)
        private readonly strokeRepository: Repository<Stroke>,
    ) {}

    async createRoom(ownerId: string) : Promise<string> {
        const inviteCode = Math.random().toString(36).substring(2, 8).toUpperCase();
        const room = this.roomRepository.create({
            inviteCode,
            ownerId,
            partnerId: null, 
        });
        await this.roomRepository.save(room);
        return inviteCode;
    }

    async joinRoom(inviteCode: string, partnerId: string) : Promise<Room> {
        const room = await this.roomRepository.findOne({ where: { inviteCode } });

        if (!room) {
            throw new NotFoundException('Room not found');
        }
        
        if (room.partnerId) {
            throw new ConflictException('Room is already full');
        }

        room.partnerId = partnerId;
        return await this.roomRepository.save(room);
    }

    async getActiveRoom(userId: string): Promise<Room | null> {
        return this.roomRepository
        .createQueryBuilder('room')
        .where('room.ownerId = :userId OR room.partnerId = :userId', { userId })
        .getOne();
    }

    async saveStroke(roomId: string, dto: SaveStrokeDTO): Promise<Stroke> {
        const room = await this.roomRepository.findOne({ where: { inviteCode: roomId.toUpperCase() } });
        if (!room) {
            throw new NotFoundException('Room not found');
        }
        const stroke = this.strokeRepository.create({
            points: dto.points,
            color: dto.color,
            strokeWidth: dto.strokeWidth,
            room,
        });
        return this.strokeRepository.save(stroke);
    }

    async getStrokes(roomId: string): Promise<Stroke[]> {
        const room = await this.roomRepository.findOne({ 
            where: { inviteCode: roomId.toUpperCase() } 
        });

        if (!room) {
            throw new NotFoundException('Room not found');
        }
        
        return this.strokeRepository.find({
            where: { room: { id: room.id } },
            order: { id: 'ASC' },
        })
    }

    private generateRoomCode(): string {
        const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
        let code = '';
        for (let i = 0; i < 6; i++) {
            code += chars.charAt(Math.floor(Math.random() * chars.length)); 
        }
        return code;
    }
}
