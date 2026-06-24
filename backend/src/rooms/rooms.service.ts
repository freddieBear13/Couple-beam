import { Injectable, NotFoundException, ConflictException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Room } from './room.entity';

@Injectable()
export class RoomsService {
    constructor(
        @InjectRepository(Room)
        private readonly roomRepository: Repository<Room>,
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
}
