import { Controller, Post, Body, HttpCode, Param, Get, Query } from '@nestjs/common';
import { RoomsService } from './rooms.service';
import { SaveStrokeDTO } from './save-stroke.dto';

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
  
  @Get('active')
  async getActiveRoom(@Query('userId') userId: string) {
    const room = await this.roomsService.getActiveRoom(userId);
    if (!room) {
      return { hasRoom: false };
    }
    return {
      hasRoom: true,
      roomId: room.id,
      code: room.inviteCode
    }
  }


  @Post(':roomId/strokes')
  @HttpCode(201)
  async saveStroke(
    @Param('roomId') roomId: string,
    @Body() dto: SaveStrokeDTO,
  ) {
    const stroke = await this.roomsService.saveStroke(roomId, dto);
    return { id: stroke.id };
  }

  @Get(':roomId/strokes')
  async getStrokes(@Param('roomId') roomId: string) {
    return this.roomsService.getStrokes(roomId);
  }
}
