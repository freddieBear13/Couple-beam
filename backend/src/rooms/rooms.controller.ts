import { Controller, Post, Body } from '@nestjs/common';
import { RoomsService } from './rooms.service';

@Controller('rooms')
export class RoomsController {
  constructor(private readonly roomsService: RoomsService) {}

  @Post('create')
  async create(@Body() body: { ownerId: string }) {
    const inviteCode = await this.roomsService.createRoom(body.ownerId);
    return { inviteCode };
  }

  @Post('join')
  async join(@Body() body: { inviteCode: string; partnerId: string }) {
    return this.roomsService.joinRoom(body.inviteCode, body.partnerId);
  }
}
