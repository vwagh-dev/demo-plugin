package io.jenkins.plugins.sample.listener;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Extension
public class JobRenameListener extends ItemListener {

    @Override
    public void onRenamed(Item item, String oldName, String newName) {
        if (item instanceof hudson.model.Job) {
            Path path = Paths.get("build_data.txt");
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = Files.newBufferedReader(path)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("JobName: " + oldName)) {
                        line = line.replace("JobName: " + oldName, "JobName: " + newName);
                    }
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (BufferedWriter bw = Files.newBufferedWriter(path)) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
