/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.codegen;

import java.io.File;
import java.util.Arrays;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

public class CodeGenMain {
    public static final String COMMAND_SOLIDITY = "solidity";
    public static final String COMMAND_GENERATE = "generate";
    public static final String COMMAND_PREFIX = COMMAND_SOLIDITY + " " + COMMAND_GENERATE;

    public enum TransactionVersion {
        V0(0),
        V1(1),

        V2(2);

        private final int v;

        TransactionVersion(int v) {
            this.v = v;
        }

        public int getV() {
            return v;
        }
    }

    public enum Version {
        V3(3),
        V2(2);

        private final int v;

        Version(int v) {
            this.v = v;
        }

        public String getVersion() {
            switch (this.v) {
                case 2:
                    return "v2";
                case 3:
                    return "v3";
                default:
                    return "";
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0
                && (args[0].equals(COMMAND_SOLIDITY) || args[0].equals(COMMAND_GENERATE))) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        CommandLine commandLine = new CommandLine(new PicocliRunner());
        commandLine.execute(args);
    }

    @Command(
            name = COMMAND_PREFIX,
            mixinStandardHelpOptions = true,
            version = "4.0",
            sortOptions = false)
    static class PicocliRunner implements Runnable {

        @Option(
                names = {"-v"},
                description = "To specified fisco-bcos-java-sdk version codeGen.",
                required = true)
        private Version version;

        @Option(
                names = {"-a", "--abiFile"},
                description = "abi file with contract definition.",
                required = true)
        private File abiFile;

        @Option(
                names = {"-b", "--binFile"},
                description =
                        "bin file with contract compiled code "
                                + "in order to generate deploy methods.",
                required = true)
        private File binFile;

        @Option(
                names = {"-s", "--smBinFile"},
                description =
                        "sm bin file with contract compiled code "
                                + "in order to generate deploy methods.",
                required = true)
        private File smBinFile;

        @Option(
                names = {"-d", "--devdoc"},
                description = "solidity devdoc file generated by NatSpec style comments.")
        private File devdocFile;

        @Option(
                names = {"-o", "--outputDir"},
                description = "destination base directory.",
                required = true)
        private File destinationFileDir;

        @Option(
                names = {"-p", "--package"},
                description = "base package name.",
                required = true)
        private String packageName;

        @Option(
                names = {"-e", "--enableAsyncCall"},
                description = "enable async call, only V3 enable.")
        private boolean enableAsyncCall = false;

        @Option(
                names = {"-t", "--txVersion"},
                description = "specify transaction version, default is 0, only V3 enable.")
        private TransactionVersion transactionVersion = TransactionVersion.V0;

        @Override
        public void run() {
            if (version.equals(Version.V2)) {
                try {
                    new org.fisco.bcos.codegen.v2.wrapper.SolidityContractGenerator(
                                    binFile,
                                    smBinFile,
                                    abiFile,
                                    devdocFile,
                                    destinationFileDir,
                                    packageName)
                            .generateJavaFiles();
                } catch (Exception e) {
                    org.fisco.bcos.codegen.v2.utils.CodeGenUtils.exitError(e);
                }
            } else if (version.equals(Version.V3)) {
                try {
                    new org.fisco.bcos.codegen.v3.wrapper.ContractGenerator(
                                    binFile,
                                    smBinFile,
                                    abiFile,
                                    devdocFile,
                                    destinationFileDir,
                                    packageName,
                                    enableAsyncCall,
                                    transactionVersion.getV())
                            .generateJavaFiles();
                } catch (Exception e) {
                    org.fisco.bcos.codegen.v3.utils.CodeGenUtils.exitError(e);
                }
            }
        }
    }
}
