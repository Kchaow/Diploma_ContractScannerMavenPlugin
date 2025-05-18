package letunov.impl;

import letunov.TestSupport;
import letunov.examples.ContractInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChecksumUtilTest extends TestSupport {

    @Test
    void getContractMethodChecksum() {
        var result = ChecksumUtil.getContractChecksum(ContractInterface.class);

        assertEquals("26c16e648caf745f411882c4bd1f5f7d54792ad9985ecfa56c3ebbfd590078cd", result);
    }
}
