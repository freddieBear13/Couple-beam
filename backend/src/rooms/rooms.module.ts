import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { RoomsService } from './rooms.service';
import { RoomsController } from './rooms.controller';
import { Room } from './room.entity';
import { Stroke } from './stroke.entity';
import { DrawingGateway } from './drawing.gateway';

@Module({
  imports: [TypeOrmModule.forFeature([Room, Stroke])],
  controllers: [RoomsController],
  providers: [RoomsService, DrawingGateway],
  exports: [RoomsService],
})
export class RoomsModule {}
