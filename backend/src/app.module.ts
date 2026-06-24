import { Module } from '@nestjs/common';
import { AuthModule } from './auth/auth.module';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from './users/user.entity';
import { Room } from './rooms/room.entity';
import { RoomsModule } from './rooms/rooms.module';

@Module({
  imports: [
    AuthModule,
    TypeOrmModule.forRoot({
      type: 'postgres',
      host: 'localhost',
      port: 5432,
      username: 'postgres',
      password: 'postgres',
      database: 'coupleapp',
      entities: [User, Room],
      synchronize: true,
    }),
    RoomsModule,
  ],
})
export class AppModule {}
