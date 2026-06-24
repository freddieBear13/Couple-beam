import { Module } from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../users/user.entity';
import { JwtModule } from '@nestjs/jwt';

@Module({
  imports: [
    TypeOrmModule.forFeature([User]),
    JwtModule.register({
      secret: 'uOjdfnCom9s8v3X5g7h2a1b4e6r9t0qLwZyN8fVj3k0p1s6x4c2',
      signOptions: { expiresIn: '7d' }
    })
  ],
  providers: [AuthService],
  controllers: [AuthController]
})
export class AuthModule {}
