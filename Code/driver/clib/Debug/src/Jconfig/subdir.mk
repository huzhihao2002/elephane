################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/Jconfig/Jconfig.c 

OBJS += \
./src/Jconfig/Jconfig.o 

C_DEPS += \
./src/Jconfig/Jconfig.d 


# Each subdirectory must supply rules for building sources it contributes
src/Jconfig/%.o: ../src/Jconfig/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I"/home/eric/github/elephant/Code/driver/clib/src/cJSON-1.4.6" -I"/home/eric/github/elephant/Code/driver/clib/src/Jconfig" -O0 -g3 -Wall -c -fmessage-length=0 -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


