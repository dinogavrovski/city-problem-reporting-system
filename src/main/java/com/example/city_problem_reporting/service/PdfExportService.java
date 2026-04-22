package com.example.city_problem_reporting.service;

import com.example.city_problem_reporting.model.Post;
import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.PostRepository;
import com.example.city_problem_reporting.repository.UserRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PdfExportService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PdfExportService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public byte[] exportUserReports(UUID userId, String username) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Post> posts = postRepository.findByUser_Id(userId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            DeviceRgb primaryColor = new DeviceRgb(37, 99, 235);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

            // ── Header ──
            Paragraph title = new Paragraph("CityFix — My Reports")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(primaryColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4);
            doc.add(title);

            Paragraph subtitle = new Paragraph("User: @" + user.getUsername() + "   |   Email: " + user.getEmail()
                    + "   |   Total reports: " + posts.size())
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            doc.add(subtitle);

            // Divider line
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                    .setMarginBottom(20));

            if (posts.isEmpty()) {
                doc.add(new Paragraph("No reports found.")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY));
            } else {
                // ── Table ──
                float[] colWidths = { 2, 4, 2, 2, 2 };
                Table table = new Table(UnitValue.createPercentArray(colWidths))
                        .useAllAvailableWidth()
                        .setMarginBottom(20);

                // Table headers
                String[] headers = { "Date", "Description", "Category", "Location", "Priority" };
                for (String header : headers) {
                    table.addHeaderCell(
                            new Cell().add(new Paragraph(header).setBold().setFontSize(10))
                                    .setBackgroundColor(primaryColor)
                                    .setFontColor(ColorConstants.WHITE)
                                    .setPadding(8)
                    );
                }

                // Table rows
                boolean alternate = false;
                for (Post post : posts) {
                    DeviceRgb rowColor = alternate
                            ? new DeviceRgb(239, 246, 255)
                            : new DeviceRgb(255, 255, 255);
                    alternate = !alternate;

                    String date = post.getCreatedAt() != null
                            ? post.getCreatedAt().format(formatter) : "-";
                    String description = post.getDescription() != null
                            ? post.getDescription() : "-";
                    String category = post.getCategory() != null
                            ? post.getCategory().replace("_", " ") : "-";
                    String location = (post.getLatitude() != null && post.getLongitude() != null)
                            ? String.format("%.4f, %.4f",
                            post.getLatitude().doubleValue(),
                            post.getLongitude().doubleValue())
                            : "Unknown";
                    String priority = post.getPriorityScore() != null && post.getPriorityScore() > 0
                            ? "High (" + post.getPriorityScore() + ")" : "Normal";

                    String[] values = { date, description, category, location, priority };
                    for (String value : values) {
                        table.addCell(
                                new Cell().add(new Paragraph(value).setFontSize(9))
                                        .setBackgroundColor(rowColor)
                                        .setPadding(6)
                        );
                    }
                }

                doc.add(table);
            }

            // ── Footer ──
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                    .setMarginTop(10));
            doc.add(new Paragraph("Generated by CityFix · " +
                    java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(8));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate PDF: " + e.getMessage());
        }
    }
}