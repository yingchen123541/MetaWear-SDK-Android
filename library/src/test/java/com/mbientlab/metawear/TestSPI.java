/*
 * Copyright 2014-2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights granted under the terms of a software
 * license agreement between the user who downloaded the software, his/her employer (which must be your
 * employer) and MbientLab Inc, (the "License").  You may not use this Software unless you agree to abide by the
 * terms of the License which can be found at www.mbientlab.com/terms.  The License limits your use, and you
 * acknowledge, that the Software may be modified, copied, and distributed when used in conjunction with an
 * MbientLab Inc, product.  Other than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this Software and/or its documentation for any
 * purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL MBIENTLAB OR ITS LICENSORS BE LIABLE OR
 * OBLIGATED UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software, contact MbientLab via email:
 * hello@mbientlab.com.
 */

package com.mbientlab.metawear;

import com.mbientlab.metawear.module.SerialPassthrough;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteElement;

import org.junit.Before;
import org.junit.Test;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by etsai on 10/6/16.
 */

public class TestSPI extends UnitTestBase {
    private SerialPassthrough.Spi spi;

    @Before
    public void setup() throws Exception {
        btlePlaform.addCustomModuleInfo(new byte[] { 0x0d, (byte) 0x80, 0x00, 0x01 });
        btlePlaform.boardInfo= MetaWearBoardInfo.RG;
        connectToBoard();

        spi = mwBoard.getModule(SerialPassthrough.class).spiData((byte) 5, (byte) 0xe);
    }

    @Test
    public void readBmi160() {
        byte[] expected= new byte[] {0x0d, (byte) 0xc2, 0x0a, 0x00, 0x0b, 0x07, 0x76, (byte) 0xe4, (byte) 0xda};

        spi.read(new byte[] {(byte) 0xda})
                .mode((byte) 3)
                .frequency(SerialPassthrough.SpiFrequency.FREQ_8_MHZ)
                .slaveSelectPin((byte) 10)
                .clockPin((byte) 0)
                .mosiPin((byte) 11)
                .misoPin((byte) 7)
                .useNativePins()
                .commit();
        assertArrayEquals(expected, btlePlaform.getLastCommand());
    }

    private static final Subscriber DATA_HANDLER= new Subscriber() {
        @Override
        public void apply(Data data, Object ... env) {
            ((Capture<byte[]>) env[0]).set(data.value(byte[].class));
        }
    };

    protected Task<Route> setupSpiStream() {
        return spi.addRoute(new RouteBuilder() {
            @Override
            public void configure(RouteElement source) {
                source.stream(DATA_HANDLER);
            }
        });
    }

    @Test
    public void bmi160Data() {
        byte[] expected= new byte[] {0x07, 0x30, (byte) 0x81, 0x0b, (byte) 0xc0};
        final Capture<byte[]> actual= new Capture<>();

        setupSpiStream().continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                task.getResult().setEnvironment(0, actual);
                return null;
            }
        });
        sendMockResponse(new byte[] {0x0d, (byte) 0x82, 0x0e, 0x07, 0x30, (byte) 0x81, 0x0b, (byte) 0xc0});

        /*
        // For TestDeserializeSPI
        btlePlaform.boardStateSuffix = "spi_stream";
        mwBoard.serialize();
        */

        assertArrayEquals(expected, actual.get());
    }
}
